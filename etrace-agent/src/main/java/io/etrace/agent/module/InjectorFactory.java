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

package io.etrace.agent.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(InjectorFactory.class);

    private static InjectorFactory INSTANCE;
    private Injector injector;

    private InjectorFactory() {
        injector = Guice.createInjector(new AgentModule());
    }

    private InjectorFactory(Injector injector) {
        this.injector = injector;
    }

    public static Injector getInjector() {
        if (INSTANCE == null) {
            INSTANCE = new InjectorFactory();
        }
        return INSTANCE.injector;
    }

    public static void setInjector(Injector injector) {
        if (INSTANCE == null) {
            INSTANCE = new InjectorFactory(injector);
        } else {
            LOGGER.error("==setInjector== ignore duplicate injector!");
        }
    }
}
