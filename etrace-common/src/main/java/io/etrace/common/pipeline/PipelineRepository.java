/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.pipeline;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.etrace.common.pipeline.PipelineConfiguration.DownStreamProp;
import io.etrace.common.pipeline.PipelineConfiguration.RouteProp;
import io.etrace.common.pipeline.PipelineConfiguration.TaskProp;
import io.etrace.common.pipeline.impl.DefaultMatchAllFilter;
import io.etrace.common.pipeline.impl.Filters;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@org.springframework.stereotype.Component
public class PipelineRepository implements BeanFactoryAware {
    public static final String PIPELINE_PATH = "pipeline";
    private final Logger LOGGER = LoggerFactory.getLogger(PipelineRepository.class);

    @Deprecated
    private final Map<String, Component> components = Maps.newHashMap();
    @Deprecated
    private final Map<String, Filter> filterMap = Maps.newHashMap();
    @Deprecated
    private final Set<Component> roots = Sets.newHashSet();

    @Getter
    private List<PipelineConfiguration> pipelines;
    @Getter
    private List<Resource> resources;
    private BeanFactory beanFactory;

    public PipelineRepository() {
    }

    public PipelineRepository(List<PipelineConfiguration> pipelines, List<Resource> resources) {
        this.pipelines = pipelines;
        this.resources = resources;
        filterMap.put(DefaultMatchAllFilter.NAME, new DefaultMatchAllFilter());
    }

    public void initAndStartUp() {
        if (null == pipelines || pipelines.isEmpty()) {
            throw new IllegalArgumentException("pipelines should not be null!");
        }

        pipelines.forEach(this::createPipeline);
        roots.forEach(this::startComponent);
    }

    public void createPipeline(PipelineConfiguration pipelineConfiguration) {
        Set<String> currentComponents = Sets.newHashSet();
        // init receivers
        if (null != pipelineConfiguration.getReceivers()) {
            pipelineConfiguration.getReceivers().forEach(channel -> {
                initComponent(pipelineConfiguration.getName(), channel, this.resources);
                currentComponents.add(channel.getName());
            });
        }

        // init components
        if (null != pipelineConfiguration.getProcessors()) {
            pipelineConfiguration.getProcessors().forEach(taskProp -> {
                initComponent(pipelineConfiguration.getName(), taskProp, this.resources);
                currentComponents.add(taskProp.getName());
            });
        }

        // init filters
        if (null != pipelineConfiguration.getFilters()) {
            pipelineConfiguration.getFilters().forEach(taskProp -> {
                try {
                    filterMap.putIfAbsent(taskProp.getName(),
                        Filters.createFilter(taskProp.getName(), taskProp.getClazz(), taskProp.getProps()));
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    LOGGER.error("failed to init Filter, name: {}, clazz: {}, props: {}", taskProp.getName(),
                        taskProp.getClazz(), taskProp.getProps());
                    throw new RuntimeException(e);
                }
            });
        }

        // init exporters
        pipelineConfiguration.getExporters().forEach(channel -> {
            initComponent(pipelineConfiguration.getName(), channel, this.resources);
            currentComponents.add(channel.getName());
        });

        Map<String, List<DownStreamProp>> routes = getRootComponents(currentComponents,
            pipelineConfiguration.getPipelines());
        currentComponents.forEach(root -> {
            roots.add(buildPipeline(root, filterMap, routes));
        });

        printPipeline(pipelineConfiguration, currentComponents);
    }

    private synchronized Component initComponent(String pipeline, TaskProp taskProp, List<Resource> resources) {
        Component component = components.get(taskProp.getName());
        if (null == component) {
            component = beanFactory.getBean(Component.class, pipeline, taskProp.getName());
            component.init(taskProp, resources);
            if (components.containsKey(taskProp.getName())) {
                LOGGER.error("==initComponent== {}", taskProp.getName());
            } else {
                components.put(taskProp.getName(), component);
            }
        }
        return component;
    }

    private void startComponent(Component component) {
        // 1. start children
        component.getDownstreams().forEach(downStream -> downStream.getComponent().forEach(this::startComponent));
        // 2. start current component
        component.startup();
    }

    private Component buildPipeline(String root, Map<String, Filter> filterMap,
                                    Map<String, List<DownStreamProp>> routeProps) {
        Component component = components.get(root);
        Optional<List<DownStreamProp>> optional = Optional.ofNullable(routeProps.get(root));
        optional.ifPresent(downStreamProps -> downStreamProps.forEach(downStreamProp -> {
            List<Component> downstreams = Lists.newArrayList();
            downStreamProp.getComponents().forEach(c -> downstreams.add(components.get(c)));
            //多条pipeline时检查是否一样，防止重复添加
            boolean exists = false;
            for (DownStream downStream : component.getDownstreams()) {
                if (downStream.getFilter().name().equals(downStreamProp.getFilter())
                    && downStream.getComponent().containsAll(downstreams) && downstreams.containsAll(
                    downStream.getComponent())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                component.addDownStream(new DownStream(filterMap.get(downStreamProp.getFilter()), downstreams));
            }
            downStreamProp.getComponents().forEach(c -> buildPipeline(c, filterMap, routeProps));
        }));
        return component;
    }

    private Map<String, List<DownStreamProp>> getRootComponents(Set<String> currentComponents,
                                                                List<RouteProp> routeProps) {
        Map<String, List<DownStreamProp>> routes = Maps.newHashMap();
        routeProps.forEach(routeProp -> {
            routes.put(routeProp.getName(), routeProp.getDownstreams());
            routeProp.getDownstreams().forEach(downStreamProp ->
                downStreamProp.getComponents().forEach(currentComponents::remove));
        });
        return routes;
    }

    private void printPipeline(PipelineConfiguration pipelineConfiguration, Set<String> currentComponents) {
        StringBuilder sb = new StringBuilder();
        for (String currentComponent : currentComponents) {
            sb.append(printPipelineInfo(currentComponent));
        }

        LOGGER.info("list the topology of this pipeline:\n"
                + "************* Pipeline [{}] ***************\n\n"
                + "{}\n"
                + "************* Pipeline [{}] ***************",
            pipelineConfiguration.getName(), sb.toString(), pipelineConfiguration.getName());

        LOGGER.info("************* Pipeline[" + pipelineConfiguration.getName() + "] ***************");
    }

    private StringBuilder printPipelineInfo(String root) {
        StringBuilder sb = new StringBuilder(128);
        Component r = components.get(root);
        sb.append("(ROOT) ").append(root).append(" (").append(Integer.toHexString(r.hashCode())).append(")\t");
        printComponent(sb, r);

        sb.append("\n");
        return sb;
    }

    private void printComponent(StringBuilder sb, Component component) {
        List<DownStream> downstreams = component.getDownstreams();
        int len = sb.length();
        for (int k = 0; k < downstreams.size(); k++) {
            DownStream downStream = downstreams.get(k);
            String filter = downStream.getFilter().name();
            List<Component> downStreamComponent = downStream.getComponent();

            if (downStreamComponent != null) {
                for (int i = 0; i < downStreamComponent.size(); i++) {
                    Component output = downStreamComponent.get(i);
                    int len2 = sb.length();
                    sb.append(" --> ")
                        .append("(")
                        .append(filter)
                        .append(")")
                        .append(output.getName())
                        .append(" (").append(Integer.toHexString(output.hashCode())).append(")\t");
                    printComponent(sb, output);
                    printNewLine(sb, i, downStreamComponent.size(), len2);
                }
            }

            printNewLine(sb, k, downstreams.size(), len);
        }
    }

    private void printNewLine(StringBuilder sb, int idx, int size, int len) {
        if (size > 1 && idx < size - 1) {
            sb.append("\r\n");
            for (int i = 0; i < len - 1; i++) {
                sb.append(" ");
            }
            sb.append("\\");
        }
    }

    @PreDestroy
    public void stop() {
        roots.forEach(this::shutdownComponent);
    }

    public Component findComponent(String name) {
        return components.get(name);
    }

    private void shutdownComponent(Component component) {
        // 1. shutdown current component
        component.stop();

        // 2. shutdown children
        for (DownStream router : component.getDownstreams()) {
            List<Component> outputs = router.getComponent();
            for (Component output : outputs) {
                shutdownComponent(output);
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
