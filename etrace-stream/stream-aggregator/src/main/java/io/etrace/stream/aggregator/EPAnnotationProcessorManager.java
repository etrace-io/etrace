package io.etrace.stream.aggregator;

import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import io.etrace.stream.aggregator.annotation.AnnotationProcessor;
import io.etrace.stream.aggregator.annotation.ProcessorFor;
import io.etrace.stream.core.util.ReflectionUtil;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

public class EPAnnotationProcessorManager {
    private Map<String, Class<? extends AnnotationProcessor>> processors = newHashMap();

    public EPAnnotationProcessorManager() {
        //scan all annotation processor
        Set<Class<? extends AnnotationProcessor>> annotationProcessor = ReflectionUtil.scannerSubType(
            AnnotationProcessor.class);
        for (Class<? extends AnnotationProcessor> clazz : annotationProcessor) {
            ProcessorFor annotation = clazz.getAnnotation(ProcessorFor.class);
            if (annotation != null) {
                processors.put(annotation.name().getSimpleName(), clazz);
            }
        }
    }

    public AnnotationProcessor createProcessor(AnnotationPart annotation, EPStatementObjectModel model,
                                               Set<String> selectItems) throws Exception {
        AnnotationProcessor processor = null;
        String name = annotation.getName();
        if (processors.containsKey(name)) {
            processor = processors.get(annotation.getName()).newInstance();
            processor.init(annotation, model);
            processor.validation(selectItems);
        }
        return processor;
    }
}
