package io.etrace.agent.monitor.jvm;

import io.etrace.agent.monitor.Executor;
import io.etrace.agent.monitor.SunManagementBean;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class EnvironmentConfig extends Executor {
    private SunManagementBean sunManagementBean = SunManagementBean.getBean();

    public EnvironmentConfig(String type) {
        super(type);
    }

    @Override
    public Map<String, String> execute() {
        Properties props = System.getProperties();
        Map<String, String> env = System.getenv();
        Map<String, String> configs = new LinkedHashMap<>();

        configs.put("os name", props.getProperty("os.name"));
        configs.put("os version", props.getProperty("os.version"));
        configs.put("os arch", props.getProperty("os.arch"));
        configs.put("memory total", (sunManagementBean.getSystemMemorySize() / 1024 / 1024) + "M");

        configs.put("available processors", Runtime.getRuntime().availableProcessors() + "");

        configs.put("java vm", props.getProperty("java.vm.name"));
        configs.put("java vm version", props.getProperty("java.vm.version"));
        configs.put("java version", props.getProperty("java.version"));
        configs.put("java home", props.getProperty("java.home"));
        configs.put("java class path", props.getProperty("java.class.path"));
        configs.put("java main class", props.getProperty("sun.java.command"));
        configs.put("jvm arguments", env.get("JAVA_OPTS"));
        configs.put("java.library.path", props.getProperty("java.library.path"));
        configs.put("java.ext.dirs", props.getProperty("java.ext.dirs"));
        configs.put("java.io.tmpdir", props.getProperty("java.io.tmpdir"));
        configs.put("java.runtime.version", props.getProperty("java.runtime.version"));
        configs.put("path", env.get("PATH"));
        configs.put("classpath", env.get("CLASSPATH"));
        configs.put("m2_home", env.get("M2_HOME"));

        return configs;
    }
}
