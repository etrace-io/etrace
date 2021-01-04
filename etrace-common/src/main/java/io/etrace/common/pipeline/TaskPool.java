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

import io.etrace.common.pipeline.impl.DefaultSyncTask;
import io.etrace.common.pipeline.impl.Task;
import io.etrace.common.sharding.ShardingStrategy;
import io.etrace.common.sharding.impl.RoundRobinStrategy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TaskPool implements BeanFactoryAware {
    private int taskSize = 1;
    private Task[] tasks;
    private ShardingStrategy shardingStrategy;
    private BeanFactory beanFactory;

    public TaskPool() {
    }

    public void init1(Component component, PipelineConfiguration.TaskProp taskProp)
        throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
        InstantiationException {
        Class<? extends Task> taskClazz = (Class<? extends Task>)Class.forName(taskProp.getClazz());
        if (null != taskProp.getProps()) {
            taskSize = (int)taskProp.getProps().getOrDefault("taskSize", 1);
        }
        if (DefaultSyncTask.class.isAssignableFrom(taskClazz)) {
            // sync task
            tasks = new Task[taskSize];
            String taskName = String.format("%s-%s-%d-%d", component.getPipeline(), component.getName(), 0, taskSize);

            tasks[0] = beanFactory.getBean(taskClazz, taskName, component, taskProp.getProps());
            //taskClazz.getConstructor(String.class, Component.class, Map.class)
            //    .newInstance();
        } else {
            tasks = new Task[taskSize];
            for (int i = 0; i < taskSize; i++) {
                // todo: HDFSProcessor.java:69 居然依赖这里的naming pattern
                String taskName = String.format("%s-%s-%d-%d", component.getPipeline(), component.getName(), i,
                    taskSize);

                tasks[i] = beanFactory.getBean(taskClazz, taskName, component, taskProp.getProps());
            }
        }

        if (taskSize > 1) {
            Optional<Object> optional = Optional.ofNullable(taskProp.getProps().get("shardingStrategy"));
            if (optional.isPresent()) {
                Class clazz = Class.forName(optional.get().toString());
                // todo?
                shardingStrategy = (ShardingStrategy)clazz.getConstructor().newInstance();
            } else {
                shardingStrategy = new RoundRobinStrategy();
            }
            shardingStrategy.init(taskSize);
        }
    }

    public void init2(Object... param) {
        for (Task task : tasks) {
            task.init(param);
        }
    }

    public void startup() {
        for (Task task : tasks) {
            task.startup();
        }
    }

    /**
     * Emit event to target task incoming queue
     */
    public void handleEvent(Object key, Object event) {
        if (this.taskSize == 0) {
            return;
        }
        int target = 0;
        if (shardingStrategy != null && this.taskSize > 1) {
            target = shardingStrategy.chooseTasks(key);
        }
        tasks[target].handleEvent(key, event);
    }

    public void stop() {
        if (tasks != null) {
            for (Task task : tasks) {
                task.stop();
            }
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
