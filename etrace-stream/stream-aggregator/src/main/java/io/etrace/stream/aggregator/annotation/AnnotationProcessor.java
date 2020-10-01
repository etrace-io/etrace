package io.etrace.stream.aggregator.annotation;

import com.espertech.esper.client.soda.AnnotationPart;
import com.espertech.esper.client.soda.EPStatementObjectModel;

import java.util.Set;

public interface AnnotationProcessor {
    void init(AnnotationPart annotation, EPStatementObjectModel model);

    void validation(Set<String> selectItems);
}
