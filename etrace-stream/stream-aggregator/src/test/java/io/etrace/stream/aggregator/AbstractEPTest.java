package io.etrace.stream.aggregator;

import com.google.common.collect.Lists;
import io.etrace.common.pipeline.Component;
import io.etrace.common.pipeline.DownStream;
import io.etrace.common.pipeline.PipelineConfiguration;
import io.etrace.common.pipeline.TaskPool;
import io.etrace.common.pipeline.impl.DefaultMatchAllFilter;
import io.etrace.stream.aggregator.config.EPConfigurationFactory;
import io.etrace.stream.aggregator.mock.MockEPReceiveTask;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.HashMap;

import static com.google.common.collect.Lists.newArrayList;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {EPConfigurationFactory.class, AbstractEPTest.class})
@TestConfiguration
public abstract class AbstractEPTest implements BeanFactoryAware {
    protected EPEngine epEngine;
    String pipeline = "test_ep";
    @Autowired
    MockEPReceiveTask mockEPReceiveTask;
    @Autowired
    @Qualifier("output")
    Component output;
    private BeanFactory beanFactory;

    @Bean
    public TaskPool taskPool() {
        TaskPool taskPool = new TaskPool();
        taskPool.setBeanFactory(beanFactory);
        return taskPool;
    }

    @Bean("output")
    public Component output() {
        return new Component(pipeline, "ep-receive");
    }

    @Bean
    public MockEPReceiveTask mockEPReceiveTask(@Autowired @Qualifier("output") Component output) {
        return new MockEPReceiveTask("test-task-poll", output, Collections.emptyMap());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Before
    public void setUp() throws Exception {
        if (epEngine != null) {
            return;
        }
        PipelineConfiguration.TaskProp taskProp = new PipelineConfiguration.TaskProp();
        taskProp.setName("test-task-poll");
        taskProp.setProps(Collections.emptyMap());
        taskProp.setClazz(MockEPReceiveTask.class.getName());
        output.init(taskProp);
        output.startup();

        Component component = new Component(pipeline, "source");
        component.addDownStream(new DownStream(
            new DefaultMatchAllFilter(),
            Lists.newArrayList(output)
        ));

        epEngine = new EPEngine("test-ep", true, component, new HashMap<>());
        epEngine.initialize();

        // add test common shaka.biz.epl.app file
        epEngine.deployModules(newArrayList("common-test.epl"));
    }

    @After
    public void tearDown() throws Exception {
        if (epEngine != null) {
            epEngine.stop();
        }
    }
}
