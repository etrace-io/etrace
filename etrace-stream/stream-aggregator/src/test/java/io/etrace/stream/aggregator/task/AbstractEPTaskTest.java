//package io.etrace.compute.aggregator.task;
//
//import org.junit.Before;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.List;
//
//import io.etrace.common.pipeline.Component;
//import me.ele.arch.shaka.aggregator.EPTask;
//import me.ele.arch.shaka.aggregator.configurer.EPConfigurer;
//import me.ele.arch.shaka.aggregator.mock.MockEPReceiveTask;
//import me.ele.arch.shaka.core.Component;
//import me.ele.arch.shaka.core.define.PipelineDefine;
//import me.ele.arch.shaka.core.pipeline.Params;
//import me.ele.arch.shaka.core.pipeline.Pipeline;
//import me.ele.arch.shaka.core.pipeline.Router;
//import me.ele.arch.shaka.core.pipeline.TaskPool;
//
//import static com.google.common.collect.Lists.newArrayList;
//
///**
// * @author io.etrace Date: 2020-04-25 Time: 17:10
// */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {EPConfigurer.class})
//public abstract class AbstractEPTaskTest {
//    protected EPTask epTask;
//
//    @Before
//    public void setUp() throws Exception {
//        Pipeline pipeline = new Pipeline() {
//            @Override
//            public String getName() {
//                return "test-pipeline";
//            }
//
//            @Override
//            public PipelineDefine getPipelineDefine() {
//                return null;
//            }
//
//            @Override
//            public void init() {
//
//            }
//
//            @Override
//            public void startup() {
//
//            }
//
//            @Override
//            public void shutdown() {
//
//            }
//        };
//        Component component = new Component("test-ep", pipeline);
//
//        Router router = new Router();
//        Component output = new Component("ep-receive", pipeline);
//        TaskPool taskPool = new TaskPool("ep-receive-task", MockEPReceiveTask.class, new Params(), output);
//        taskPool.init();
//        output.setTaskPool(taskPool);
//        output.init();
//        output.startup();
//
//        router.setOutputs(newArrayList(output));
//
//        component.setRouters(newArrayList(router));
//
//        Params params = new Params();
//        List<String> modules = newArrayList("common-test.epl");
//        modules.addAll(deployModules());
//        params.put("epls", modules);
//
//        epTask = new EPTask("epTask", params, component);
//
//        epTask.init();
//        epTask.startup();
//
//    }
//
//    abstract List<String> deployModules();
//
//}
