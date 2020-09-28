package io.etrace.stream.aggregator.config;

import io.etrace.stream.aggregator.annotation.AggregatorFunction;
import io.etrace.stream.aggregator.annotation.UserDefineFunction;
import io.etrace.stream.core.util.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Set;

public class EPConfigurationFactory {

    public static com.espertech.esper.client.Configuration createEPConfiguration() {
        com.espertech.esper.client.Configuration configuration = new com.espertech.esper.client.Configuration();
        configuration.addAnnotationImport("io.etrace.stream.aggregator.annotation.*");
        configuration.addImport("io.etrace.stream.aggregator.function.*");

        configuration.getEngineDefaults().getThreading().setListenerDispatchPreserveOrder(false);

        // use FlushEvent control context then send thread will block if listener block. if internalTimerEnabled,
        // send thread won't be blocked
        configuration.getEngineDefaults().getThreading().setInternalTimerEnabled(false);

        //        configuration.getEngineDefaults().getThreading().setThreadPoolOutbound(true);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolInbound(true);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolInboundCapacity(1);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolInboundNumThreads(1);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolOutboundCapacity(3000);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolOutboundNumThreads(2);

        //        configuration.getEngineDefaults().getThreading().setThreadPoolTimerExec(true);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolTimerExecCapacity(3000);
        //        configuration.getEngineDefaults().getThreading().setThreadPoolTimerExecNumThreads(2);

        //load plugin aggregator function factory
        Set<Class<?>> functionFactorySet = ReflectionUtil.getTypesAnnotatedWith(AggregatorFunction.class);
        for (Class<?> functionFactory : functionFactorySet) {
            AggregatorFunction aggregatorFunction = functionFactory.getAnnotation(AggregatorFunction.class);
            if (aggregatorFunction != null) {
                configuration.addPlugInAggregationFunctionFactory(aggregatorFunction.name(), functionFactory.getName());
            }
        }

        // load user define function
        Set<Method> userDefineFunctions = ReflectionUtil.getMethodsAnnotatedWith(UserDefineFunction.class);
        for (Method method : userDefineFunctions) {
            UserDefineFunction userDefineFunction = method.getAnnotation(UserDefineFunction.class);
            configuration.addPlugInSingleRowFunction(userDefineFunction.name(), method.getDeclaringClass().getName(),
                method.getName());
        }
        return configuration;
    }
}
