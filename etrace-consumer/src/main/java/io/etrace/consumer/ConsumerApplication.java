/*
 * Copyright 2019 etrace.io
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

package io.etrace.consumer;

import io.etrace.common.pipeline.PipelineRepository;
import io.etrace.common.pipeline.impl.DefaultPipelineLoader;
import io.etrace.consumer.config.ConsumerProperties;
import io.etrace.consumer.storage.hadoop.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication
public class ConsumerApplication implements BeanFactoryAware {
    public final Logger LOGGER = LoggerFactory.getLogger(ConsumerApplication.class);
    @Autowired
    private ConsumerProperties consumerProperties;
    private BeanFactory beanFactory;

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @PostConstruct
    public void startup() {
        //AgentConfiguration.setDebugMode(true);
        try {
            PipelineRepository pipelineRepository = beanFactory.getBean(PipelineRepository.class,
                new DefaultPipelineLoader().load(), consumerProperties.getResources());
            pipelineRepository.initAndStartUp();
        } catch (Exception e) {
            LOGGER.error("fail to start up: ", e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        System.out.println("===============start shutdown");
        FileSystemManager.getFileSystem().close();
        System.out.println("===============shutdown success!");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
