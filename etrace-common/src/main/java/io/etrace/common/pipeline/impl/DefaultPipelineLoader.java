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

package io.etrace.common.pipeline.impl;

import com.google.common.collect.Lists;
import io.etrace.common.pipeline.PipelineConfiguration;
import io.etrace.common.pipeline.PipelineLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static io.etrace.common.pipeline.PipelineRepository.PIPELINE_PATH;

public class DefaultPipelineLoader implements PipelineLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPipelineLoader.class);

    private File file;

    public DefaultPipelineLoader() throws FileNotFoundException {
        this(PIPELINE_PATH);
    }

    /**
     * default path is "pipeline"
     */
    public DefaultPipelineLoader(String pipelinePath) throws FileNotFoundException {
        try {
            this.file = ResourceUtils.getFile("classpath:" + pipelinePath);
        } catch (FileNotFoundException e) {
            this.file = ResourceUtils.getFile("file:" + pipelinePath);
            //this.file = new File(pipelinePath);
        }
    }

    @Override
    public List<PipelineConfiguration> load() throws Exception {
        List<PipelineConfiguration> pipelines = Lists.newArrayList();
        File[] files = file.listFiles();

        LOGGER.info("going to load PipelineConfiguration files from [{}]", file.getAbsolutePath());

        if (null != files) {
            for (File file : files) {
                if (file.getName() .endsWith(".yaml") || file.getName() .endsWith(".yml")) {
                    Yaml yaml = new Yaml();
                    InputStream initialStream = new FileInputStream(file);
                    PipelineConfiguration pipelineConfiguration = yaml.loadAs(initialStream,
                        PipelineConfiguration.class);
                    if (null == pipelineConfiguration) {
                        LOGGER.warn("won't load pipeline from {} as its configuration is null.", file.getName());
                    } else if (!pipelineConfiguration.isEnable()) {
                        LOGGER.warn("won't load pipeline from {} as its configuration is disabled", file.getName());
                    } else {
                        pipelineConfiguration.setName(file.getName().substring(0, file.getName().indexOf(".")));
                        pipelines.add(pipelineConfiguration);
                    }
                } else {
                    LOGGER.warn("File [{}] ignored. Only accept '*.yaml' or '*.yml' yaml configuration file.",
                        file.getName());
                }
            }
        }
        return pipelines;
    }
}
