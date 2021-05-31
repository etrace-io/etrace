package io.etrace.stream.aggregator;

import com.google.common.collect.Lists;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static io.etrace.common.pipeline.PipelineRepository.PIPELINE_PATH;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EPTask extends DefaultAsyncTask implements Processor {
    private final static Logger LOGGER = LoggerFactory.getLogger(EPTask.class);
    private static final long CHECK_FLUSH_INTERVAL = 5000;
    private final EPEngine epEngine;
    private String[] epls;
    private ScheduledExecutorService scheduledExecutorService;

    @Value("${etrace.stream.outputDetailEpl:false}")
    private final boolean outputDetailEpl = false;

    public EPTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        epEngine = new EPEngine(name, outputDetailEpl, component, params);

        Optional.ofNullable(params.get("epls")).ifPresent(values -> epls = values.toString().split(","));
    }

    @Override
    public void processEvent(Object key, Object event) {
        epEngine.sendEvent(event);
    }

    @Override
    public void init(Object... param) {
        super.init(param);
        epEngine.initialize();
        List<String> eplModels = newArrayList();
        try {
            if (null == epls) {
                String epl = PIPELINE_PATH + File.separator + component.getPipeline() + File.separator + component
                    .getName() + ".sql";
                LOGGER.info("'epls' parameter not set, [{}] going to load default epl [{}]", this.getName(), epl);
                eplModels.add(epl);
            } else {
                for (String epl : epls) {
                    URL resource = ClassLoader.getSystemResource(epl);
                    if (null == resource) {
                        throw new IllegalArgumentException("resource <" + epl + "> not found");
                    }
                    URI uri = resource.toURI();
                    if ("jar".equals(uri.getScheme())) {
                        Map<String, String> env = newHashMap();
                        env.put("create", "true");
                        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
                            String jarPath = uri.getSchemeSpecificPart();
                            String jarPathPath = jarPath.substring(jarPath.indexOf("!/") + 2);
                            Path path = fileSystem.getPath(jarPathPath);
                            eplModels.addAll(loadFile(epl, path));
                            //if (Files.isDirectory(path)) {
                            //    eplModels.addAll(Files.list(path)
                            //        .map(p -> epl + File.separator + p.getFileName().toString())
                            //        .collect(Collectors.toList()));
                            //} else {
                            //    LOGGER.info("[{}] going to load epl [{}] from jar [{}]", this.getName(), epl,
                            //    jarPath);
                            //    // relative path
                            //    eplModels.add(epl);
                            //}
                        }
                    } else {
                        Path path = Paths.get(resource.toURI());
                        eplModels.addAll(loadFile(epl, path));
                    }
                }
            }

            LOGGER.info("[{}] loaded epl modules are: \n{}", this.getName(), eplModels);
            epEngine.deployModules(eplModels);
        } catch (Exception ex) {
            throw new RuntimeException("deploy modules failed:" + name, ex);
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(1, (runnable) -> {
            Thread thread = new Thread(runnable, name + "-EPTask-Flush-Thread");
            return thread;
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // 某些罕见的 events由于collector sharding策略  shaka esper处理的数据时有时无 需要定时flush
            // handleEvent event放入ringbuffer中 由sender线程执行checkFlush
            // 直接调用esper checkFlushEvent会有线程问题 flushCounter kafkaSink blockStore 都是threadLocal
            handleEvent("checkFlushEvent", new CheckFlushEvent());
            LOGGER.debug("{} send check flush event", name);
        }, CHECK_FLUSH_INTERVAL, CHECK_FLUSH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private List<String> loadFile(String epl, Path path) throws IOException {
        List<String> files = Lists.newArrayList();
        if (Files.isDirectory(path)) {

            for (Path path1 : Files.list(path).collect(Collectors.toList())) {
                if (Files.isDirectory(path1)) {
                    files.addAll(loadFile(epl + File.separator + path1.getFileName(), path1));
                } else {
                    String file;
                    if (epl.endsWith(File.separator)) {
                        file = epl + path1.getFileName().toString();
                    } else {
                        file = epl + File.separator + path1.getFileName().toString();
                    }
                    LOGGER.info("[{}] going to load epl [{}] from file [{}] in directory [{}]",
                        this.getName(), epl, file, path);
                    files.add(file);
                }
            }
        } else {
            LOGGER.info("[{}] going to load epl [{}] from file [{}]", this.getName(), epl, path);
            files.add(epl);
        }
        return files;
    }

    @Override
    public void stop() {
        super.stop();

        if (epEngine != null) {
            epEngine.stop(); //ep engine destroy after some time
            LOGGER.info("Esper task <{}> shutdown successfully!", this.getName());
        }
    }
}
