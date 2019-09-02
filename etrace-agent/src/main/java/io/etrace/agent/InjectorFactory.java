package io.etrace.agent;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class InjectorFactory {
    private static InjectorFactory INSTANCE = new InjectorFactory();
    private Injector injector = null;

    private InjectorFactory() {
        if (injector == null) {
            injector = Guice.createInjector(new AgentModule());
        }
    }

    public static Injector getInjector() {
        return INSTANCE.injector;
    }

    public static void setInjector(Injector injector) {
        INSTANCE.injector = injector;
    }
}
