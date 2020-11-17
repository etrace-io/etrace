package io.etrace.stream.aggregator;

import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.Processor;
import io.etrace.common.pipeline.impl.DefaultAsyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.io.File;
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
    private EPEngine epEngine;
    private String[] epls;
    private ScheduledExecutorService scheduledExecutorService;

    public EPTask(String name, Component component, Map<String, Object> params) {
        super(name, component, params);

        epEngine = new EPEngine(name, component, params);

        Optional.ofNullable(params.get("epls")).ifPresent(values -> epls = values.toString().split(","));
    }

    @Override
    public void processEvent(Object key, Object event) {
        epEngine.sendEvent(event);
        //        System.out.println("EPTask:" + event.getClass());
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
                eplModels.add(epl);
            } else {
                for (String epl : epls) {
                    URL resource = ClassLoader.getSystemResource(epl);
                    if (null == resource) {
                        throw new IllegalArgumentException("resource <" + epl + "> not found");
                    }
                    URI uri = resource.toURI();
                    if (uri.getScheme().equals("jar")) {
                        Map<String, String> env = newHashMap();
                        env.put("create", "true");
                        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
                            String jarPath = uri.getSchemeSpecificPart();
                            String jarPathPath = jarPath.substring(jarPath.indexOf("!/") + 2);
                            Path path = fileSystem.getPath(jarPathPath);
                            if (Files.isDirectory(path)) {
                                eplModels.addAll(Files.list(path)
                                    .map(p -> epl + File.separator + p.getFileName().toString())
                                    .collect(Collectors.toList()));
                            } else {
                                // relative path
                                eplModels.add(epl);
                            }
                        }

                    } else {
                        Path path = Paths.get(resource.toURI());
                        if (Files.isDirectory(path)) {
                            eplModels.addAll(Files.list(path)
                                .map(p -> epl + File.separator + p.getFileName().toString())
                                .collect(Collectors.toList()));
                        } else {
                            eplModels.add(epl);
                        }
                    }
                }
            }
            epEngine.deployModules(eplModels);
        } catch (Exception ex) {
            throw new RuntimeException("deploy modules failed:" + name, ex);
        }

        scheduledExecutorService = Executors.newScheduledThreadPool(1, (runnable) -> {
            Thread thread = new Thread(runnable, name + "-EPTask-Flush-Thread");
            return thread;
        });

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            // soa_proxy, star events由于collector sharding策略  shaka esper处理的数据时有时无 需要定时flush
            // handleEvent event放入ringbuffer中 由sender线程执行checkFlush
            // 直接调用esper checkFlushEvent会有线程问题 flushCounter kafkaSink blockStore 都是threadLocal
            handleEvent("checkFlushEvent", new CheckFlushEvent());
            LOGGER.debug("{} send check flush event", name);
        }, CHECK_FLUSH_INTERVAL, CHECK_FLUSH_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();

        if (epEngine != null) {
            epEngine.stop(); //ep engine destroy after some time
            LOGGER.info(" ep task <{}> shutdown successfully!", this.getName());
        }
    }
}
