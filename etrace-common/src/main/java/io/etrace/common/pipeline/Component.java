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

import io.etrace.common.message.trace.MessageHeader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@EqualsAndHashCode
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Component {
    private final static Logger LOGGER = LoggerFactory.getLogger(Component.class);

    @EqualsAndHashCode.Include
    private String pipeline;
    @EqualsAndHashCode.Include
    private String name;
    private List<DownStream> downstreams = new ArrayList<>();
    private boolean timeTick;
    @Autowired
    private TaskPool taskPool;

    public Component(String pipeline, String name) {
        this.pipeline = pipeline;
        this.name = name;
    }

    public void init(Object... param) {
        PipelineConfiguration.TaskProp taskProp = (PipelineConfiguration.TaskProp)param[0];
        try {
            this.taskPool.init1(this, taskProp);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LOGGER.error("failed to init Component: {}", name, e);
            throw new RuntimeException(e);
        }

        this.taskPool.init2(param);
        if (null != taskProp.getProps()) {
            timeTick = (boolean)Optional.ofNullable(taskProp.getProps().get("timeTick")).orElse(false);
        }

    }

    public void startup() {
        taskPool.startup();
        LOGGER.info("Startup pipeline [{}] system component [{}] successfully!", pipeline, name);
    }

    public void emit(Object key, Object event) {
        this.taskPool.handleEvent(key, event);
    }

    public void addDownStream(DownStream downStream) {
        downstreams.add(downStream);
    }

    public void stop() {
        // ensure inputs all closed
        //todo 这里有点不一样
        try {
            taskPool.stop();
        } catch (Throwable e) {
            LOGGER.error("shutdown system component [{}] error:", name, e);
        }
        LOGGER.info("Shutdown pipeline [{}] system component [{}] successfully!", pipeline, name);
    }

    // todo:  待梳理
    public void dispatch(@Nonnull Object key, @Nonnull Object event) {
        if (!downstreams.isEmpty()) {
            for (DownStream downStream : downstreams) {
                Object obj = downStream.getFilter().match(key);
                if (null != obj) {
                    for (Component component : downStream.getComponent()) {
                        component.emit(key, event);
                    }
                }
            }
        } else {
            LOGGER.warn("Pipeline [{}], downstreams are empty. You should check the its configuration", pipeline);
        }
    }

    public void routeToFirstComponent(MessageHeader key, Object data) {
        if (!downstreams.isEmpty()) {
            for (DownStream downStream : downstreams) {
                // todo 这里太针对性处理了
                Object obj = downStream.getFilter().match(key.getMessageType());
                if (null != obj) {
                    for (Component component : downStream.getComponent()) {
                        component.emit(key, data);
                    }
                    return;
                }
            }
        } else {
            LOGGER.warn("Pipeline [{}], component [{}] dispatch is empty, event miss.", pipeline, name);
        }
    }
}
