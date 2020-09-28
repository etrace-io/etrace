package io.etrace.stream.core.util;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

public class ReflectionUtil {
    private static Reflections reflections;

    static {
        reflections = new Reflections(
            new ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("io.etrace.stream"))
                .addScanners(new TypeAnnotationsScanner(), new SubTypesScanner(), new MethodAnnotationsScanner()));
    }

    public static <T> Set<Class<? extends T>> scannerSubType(Class<T> subType) {
        return reflections.getSubTypesOf(subType);
    }

    public static Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflections.getTypesAnnotatedWith(annotation);
    }

    public static Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
        return reflections.getMethodsAnnotatedWith(annotation);
    }

}
