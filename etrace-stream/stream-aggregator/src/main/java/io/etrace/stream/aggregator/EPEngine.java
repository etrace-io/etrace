package io.etrace.stream.aggregator;

import com.espertech.esper.client.*;
import com.espertech.esper.client.annotation.Name;
import com.espertech.esper.client.deploy.*;
import com.espertech.esper.client.soda.*;
import com.google.common.collect.Sets;
import io.etrace.common.exception.EsperConfigException;
import io.etrace.common.pipeline.Component;
import io.etrace.stream.aggregator.annotation.AnnotationProcessor;
import io.etrace.stream.aggregator.annotation.MetricProcessor;
import io.etrace.stream.aggregator.annotation.TimeWindow;
import io.etrace.stream.aggregator.config.EPConfigurationFactory;
import io.etrace.stream.aggregator.expression.EPClause;
import io.etrace.stream.aggregator.expression.EPClauseFactory;
import io.etrace.stream.aggregator.plugin.GroupCountAggregatorFactory;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class EPEngine {

    public static final String EPL_META_DATA = "epl_meta_data";
    public static final String EPL_METRIC_PROCESSORS = "epl_metric_processors";
    public static final Pattern PARAM_PATTERN = Pattern.compile("\\{(.*?)}");
    public static final String TIMESTAMP = "timestamp";
    public static final String TIME_MINUTES = "timeMinutes";
    public static final String UNKNOWN = "unknown";

    public static final int DEFAULT_INTERVAL = 5000;
    private final static Logger LOGGER = LoggerFactory.getLogger(EPEngine.class);
    private final static String COMMON_EPL = "common.epl";
    private final static int DEFAULT_TIME_WINDOW = 5;
    EPAdministrator epAdmin;
    private long flushSize;
    private int flushInterval;
    private long timestamp = System.currentTimeMillis();
    private EPEngineStat stat;
    private Component component;
    private EPServiceProvider epServiceProvider;
    private EPRuntime epRuntime;
    private StatementAwareUpdateListener listener;
    private String name;
    private Configuration configuration;
    private EPAnnotationProcessorManager epAnnotationProcessorManager;
    private AtomicBoolean initialised;
    private Map<String, Object> params;

    private Map<Class, String> eventTypeCache;

    EPEngine(String name, Component component, Map<String, Object> params) {
        this.name = name;
        this.component = component;
        this.configuration = EPConfigurationFactory.createEPConfiguration();
        this.epAnnotationProcessorManager = new EPAnnotationProcessorManager();
        this.flushSize = (long)Optional.ofNullable(params.get("flush.size")).orElse(300L);
        this.flushInterval = (int)Optional.ofNullable(params.get("flush.interval")).orElse(1000);

        this.eventTypeCache = new ConcurrentHashMap<>();
        this.stat = new EPEngineStat(name, component);
        initialised = new AtomicBoolean(false);
        this.params = params;
    }

    public synchronized void initialize() {
        if (initialised.get()) {
            return;
        }

        LOGGER.info("Initializing ep engine [{}].", name);
        try {
            epServiceProvider = EPServiceProviderManager.getProvider(name, this.configuration);
            epRuntime = epServiceProvider.getEPRuntime();
            epAdmin = epServiceProvider.getEPAdministrator();
            listener = new EPAggregatorEventListener(component, params);

            deployModules(newArrayList(COMMON_EPL));

        } catch (Exception e) {
            LOGGER.error("initialize ep engine [{}] error:", name, e);
            throw new EsperConfigException(e.getMessage(), e);
        }

        // also reset timestamp
        timestamp = System.currentTimeMillis();

        initialised.set(true);
        LOGGER.info("Finished initialize ep engine [{}].", name);
    }

    public void sendEvent(Object event) {
        if (event == null) {
            return;
        }
        if (event instanceof List) {
            List events = (List)event;
            for (Object obj : events) {
                emit(obj);
            }
        } else {
            emit(event);
        }

        checkFlush();
    }

    private void emit(Object event) {
        Counter counter;
        epRuntime.sendEvent(event);

        String eventType = eventTypeCache.get(event.getClass());
        if (eventType == null) {
            eventType = event.getClass().getSimpleName();
            eventTypeCache.put(event.getClass(), eventType);
        }

        counter = stat.getCounter(eventType);
        if (counter == null) {
            counter = stat.registerEventCounter(eventType);
        }
        counter.increment();
    }

    private void checkFlush() {
        final long currentCount = FlushCounter.get();
        if (currentCount > flushSize || System.currentTimeMillis() > timestamp + flushInterval) {
            flush();
            LOGGER.debug("Esper Flush, engine:<{}>, size:<{}>, flushSize:<{}>", name, currentCount, flushSize);
        }
    }

    public void flush() {
        emit(new FlushEvent());
        FlushCounter.reset();
        timestamp = System.currentTimeMillis();
    }

    // files are relative paths on classpath
    public void deployModules(List<String> files) throws Exception {
        List<Module> modules = newArrayList();
        EPDeploymentAdmin deploymentAdmin = epAdmin.getDeploymentAdmin();
        for (String file : files) {
            LOGGER.info("Load module [{}]", file);
            Module module = deploymentAdmin.read(file);
            modules.add(module);
        }
        DeploymentOrder deploymentOrder = deploymentAdmin.getDeploymentOrder(modules, new DeploymentOrderOptions());
        for (Module module : deploymentOrder.getOrdered()) {
            LOGGER.info("EPEngine deploy module [{}]", module.getName());
            module.getImports().forEach(epAdmin.getConfiguration()::addImport);

            for (int i = 0; i < module.getItems().size(); i++) {
                ModuleItem item = module.getItems().get(i);
                if (!item.isCommentOnly()) {
                    String policyName = getPolicyName(item.getExpression());
                    createEPL(policyName == null ? module.getName() + "_epl" : policyName, item.getExpression());
                }
            }
        }
    }

    private String getPolicyName(String text) {
        EPStatementObjectModel objectModel = compileEPL(text);

        // if create context, using context name for policy name
        if (objectModel.getCreateContext() != null && objectModel.getCreateContext().getContextName() != null) {
            return objectModel.getCreateContext().getContextName();
        }

        List<AnnotationPart> annotationParts = objectModel.getAnnotations();
        for (AnnotationPart annotationPart : annotationParts) {
            if (Name.class.getSimpleName().equals(annotationPart.getName())) {
                for (AnnotationAttribute attribute : annotationPart.getAttributes()) {
                    if ("value".equals(attribute.getName())) {
                        return (String)attribute.getValue();
                    }
                }
            }
        }
        return null;
    }

    public synchronized EPStatement createEPL(String name, String epl) throws Exception {
        EPStatementObjectModel model = compileEPL(epl);

        EPLMetaData eplMetaData = new EPLMetaData();

        Map<String, Object> userObject = newHashMap();
        userObject.put(EPL_META_DATA, eplMetaData);

        List<AnnotationPart> annotations = model.getAnnotations();

        EPClause epSelectClause = EPClauseFactory.buildSelectClause(model);
        EPClause epGroupByClause = EPClauseFactory.buildGroupByClaus(model);

        int timeWindow = DEFAULT_TIME_WINDOW;

        if (annotations != null && annotations.size() > 0) {
            List<MetricProcessor> processors = newArrayList();
            for (AnnotationPart annotation : annotations) {
                String annotationName = annotation.getName();
                if (TimeWindow.class.getSimpleName().equals(annotationName)) {
                    List<AnnotationAttribute> attrs = annotation.getAttributes();
                    for (AnnotationAttribute attr : attrs) {
                        if ("value".equals(attr.getName())) {
                            timeWindow = (int)attr.getValue();
                        }
                    }
                } else {
                    AnnotationProcessor processor = epAnnotationProcessorManager.createProcessor(annotation, model,
                        epSelectClause.asNames());
                    if (processor instanceof MetricProcessor) {
                        processors.add((MetricProcessor)processor);
                    }
                }
            }
            userObject.put(EPL_METRIC_PROCESSORS, processors);
        }

        // ugly
        if (isGroupByStatement(model)) {
            checkGroupByItems(model, epSelectClause.items(), epGroupByClause.items());

            addGroupCount(model, epSelectClause);

            addOutputLimitClause(model, timeWindow);

        }

        LOGGER.info("create epl: " + model.toEPL());

        EPStatement epStatement = epAdmin.create(model, name, userObject);
        if (listener != null) {
            epStatement.addListener(listener);
        } else {
            //todo: only for DEBUG! remove this when commit!
            System.out.println("xxxxxxxxxxxxxxxxxxx");
        }

        epStatement.start();
        return epStatement;
    }

    public EPStatementObjectModel compileEPL(String epl) {
        return epAdmin.compileEPL(epl);
    }

    private boolean isGroupByStatement(EPStatementObjectModel model) {
        return model.getSelectClause() != null && model.getSelectClause().getSelectList() != null
            && !model.getSelectClause().getSelectList().isEmpty() && model.getGroupByClause() != null;
    }

    private void checkGroupByItems(EPStatementObjectModel model, Set<String> noAggregateExpressions,
                                   Set<String> groupByItems) {
        if (!noAggregateExpressions.equals(groupByItems)) {
            Set<String> extraNoAggs = Sets.difference(noAggregateExpressions, groupByItems);
            Set<String> extraGroupBys = Sets.difference(groupByItems, noAggregateExpressions);
            StringBuilder msg = new StringBuilder();
            if (!extraNoAggs.isEmpty()) {
                msg.append("No aggregate functions ").append(extraNoAggs).append(" not listed in group by clause. ");
            }
            if (!extraGroupBys.isEmpty()) {
                msg.append("GroupBy items ").append(extraGroupBys).append(" not listed in select clauses.");
            }
            msg.append(" Epl:").append(model.toEPL());
            throw new EsperConfigException(msg.toString());
        }
    }

    private void addGroupCount(EPStatementObjectModel model, EPClause epClause) {
        if (!hasGroupCountMethod(epClause)) {
            //todo group_count的作用不是特别理解
            PlugInProjectionExpression plugInProjectionExpression = new PlugInProjectionExpression();
            plugInProjectionExpression.setFunctionName(GroupCountAggregatorFactory.METHOD_NAME);
            SelectClauseExpression selectClauseExpression = new SelectClauseExpression();
            selectClauseExpression.setExpression(plugInProjectionExpression);
            model.getSelectClause().getSelectList().add(selectClauseExpression);
        }
    }

    private boolean hasGroupCountMethod(EPClause epClause) {
        return epClause.asNames().contains(GroupCountAggregatorFactory.METHOD_NAME);
    }

    private void addOutputLimitClause(EPStatementObjectModel model, int timeWindow) {
        if (!hasContextName(model)) {
            String ctxName = createTimeWindow(timeWindow);
            model.setContextName(ctxName);
            model.setOutputLimitClause(buildOutputLimitClause());
        }
    }

    private boolean hasContextName(EPStatementObjectModel model) {
        return model.getContextName() != null;
    }

    public EPStatement getStatement(String statementName) {
        return epAdmin.getStatement(statementName);
    }

    protected String createTimeWindow(int interval) {
        String template = String.format(
            "create context ctx_%dsec start @now end io.etrace.stream.aggregator.FlushEvent", interval);
        EPStatementObjectModel model = compileEPL(template);
        String name = model.getCreateContext().getContextName();
        EPStatement statement = epAdmin.getStatement(name);

        //if context not exist, create new one
        if (statement == null) {
            epAdmin.create(model, name);
        }
        return name;
    }

    private OutputLimitClause buildOutputLimitClause() {
        OutputLimitClause outputLimitClause = new OutputLimitClause();
        outputLimitClause.setSelector(OutputLimitSelector.SNAPSHOT);
        outputLimitClause.setUnit(OutputLimitUnit.CONTEXT_PARTITION_TERM);
        return outputLimitClause;
    }

    public void stop() {
        if (epServiceProvider != null) {
            LOGGER.info("Start to shutdown EPEngine");
            emit(new FlushEvent());

            epServiceProvider.destroy();
            epServiceProvider = null;
            LOGGER.info("Shutdown EPEngine [{}] complete", name);
        }
    }

}
