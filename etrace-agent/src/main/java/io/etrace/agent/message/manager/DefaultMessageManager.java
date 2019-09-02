package io.etrace.agent.message.manager;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.message.CallStackProducer;
import io.etrace.agent.message.DefaultTraceContext;
import io.etrace.agent.message.IdFactory;
import io.etrace.common.Constants;
import io.etrace.common.exception.TooManyRedisException;
import io.etrace.common.message.ConfigManger;
import io.etrace.common.message.MessageManager;
import io.etrace.common.modal.*;
import io.etrace.common.modal.impl.EventImpl;
import io.etrace.common.modal.impl.TransactionImpl;

import java.util.*;

import static io.etrace.common.Constants.ROOT_RPC_ID;

public class DefaultMessageManager implements MessageManager {

    public static final Set<String> ignoreEagleTraceType = new HashSet<>();

    /**
     * 该集合存储了一些特殊中间件的transaction type,原因如下: eMonitor界面上如果出现了通过jdbc,amqp,jedis调用集团的组件的时候 需要添加一个链接到鹰眼的点击按钮
     * 所以需要在tag里加上鹰眼id(如果存在的话),以此方便可以跳转
     */
    public static final Set<String> addEagleTraceIdType = new HashSet<>();

    // 以下的transaction type自身替换成集团组件后会自行调用鹰眼
    // 所以trace内部无需再调用鹰眼
    static {

        addEagleTraceIdType.add(Constants.SQL);
        addEagleTraceIdType.add(Constants.RMQ_PRODUCE);
        addEagleTraceIdType.add(Constants.RMQ_CONSUME);
        addEagleTraceIdType.add(Constants.REDIS_TYPE);

        ignoreEagleTraceType.add(Constants.SERVICE);
        ignoreEagleTraceType.add(Constants.CALL);
        ignoreEagleTraceType.add("System");// heartbeat
        ignoreEagleTraceType.add("Platform");// heartbeat
        // dal
        ignoreEagleTraceType.add("SEQ");
        ignoreEagleTraceType.add("GLOBALID");
        ignoreEagleTraceType.add("DAL");
        ignoreEagleTraceType.add("TRANS");
        ignoreEagleTraceType.add(Constants.SQL);

        /**
         * url入口拦截
         */
        ignoreEagleTraceType.add(Constants.URL);

        ignoreEagleTraceType.addAll(addEagleTraceIdType);
    }

    private CallStackProducer producer;
    private IdFactory idFactory;
    private ConfigManger configManger;
    private ThreadLocal<Context> context;
    private ThreadLocal<Boolean> isImport = new ThreadLocal<>();
    @Inject
    public DefaultMessageManager(CallStackProducer callStackProducer, IdFactory idFactory, ConfigManger configManger) {
        this(callStackProducer, idFactory, configManger, true);
    }

    DefaultMessageManager(CallStackProducer callStackProducer, IdFactory idFactory, ConfigManger configManger,
                          boolean createNewContext) {
        this.configManger = configManger;
        this.producer = callStackProducer;
        this.idFactory = idFactory;
        if (createNewContext) {
            this.context = new ThreadLocal<>();
        }
    }

    private static boolean ignoreEagleType(Transaction transaction) {
        return ignoreEagleTraceType.contains(transaction.getType());
    }

    @Override
    public void addRedis(String url, String command, long duration, boolean succeed, String redisType,
                         RedisResponse[] responses) {
        Context ctx = getContext();
        if (ctx == null) {
            return;
        }
        try {
            ctx.mergeRedisStats(url, command, duration, succeed, responses, redisType);
        } catch (TooManyRedisException e) {
        }
    }

    @Override
    public void add(Message message) {
        Context ctx = getContext();
        if (ctx != null) {
            ctx.add(message);
        }
    }

    @Override
    public void end(Transaction transaction) {
        Context ctx = getContext();
        if (ctx != null) {
            if (ctx.end(transaction)) {
                removeContext();
            }
        }
    }

    @Override
    public ConfigManger getConfigManager() {
        return configManger;
    }

    @Override
    public void start(Transaction transaction) {
        Context ctx = getContext();
        if (ctx != null) {
            ctx.start(transaction);
        }
    }

    @Override
    public void shutdown() {
        configManger.shutdown();
        producer.shutdown();
    }

    @Override
    public boolean hasTransaction() {
        Context ctx = context.get();
        if (ctx == null) {
            return false;
        }
        return ctx.getRoot() != null;
    }

    @Override
    public void setup() {
        context.set(new Context());
    }

    @Override
    public void setup(String requestId, String rpcId) {
        context.set(new Context(requestId, rpcId));
    }

    @Override
    public TraceContext exportContext() {
        Context ctx = getContext();
        if (ctx != null) {
            return new DefaultTraceContext(ctx);
        }
        return null;
    }

    @Override
    public void importContext(TraceContext ctx) {
        if (ctx != null && ctx.getCtx() instanceof Context) {
            context.set((Context)ctx.getCtx());
            isImport.set(true);
        }
    }

    @Override
    public String getClientAppId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getClientAppId();
        }
        return Constants.UNKNOWN_APP_ID;
    }

    @Override
    public boolean hasContext() {
        return context != null && context.get() != null;
    }

    @Override
    public boolean isImportContext() {
        return isImport.get() != null && isImport.get();
    }

    @Override
    public void reset() {
        Context ctx = getContext();
        if (ctx != null) {
            if (ctx.totalDuration == 0) {
                ctx.stack.clear();
                ctx.knownExceptions.clear();
                removeContext();
            } else {
                ctx.knownExceptions.clear();
            }
        }
    }

    @Override
    public void removeContext() {
        if (context != null) {
            context.remove();
        }
        isImport.remove();
    }

    @Override
    public String nextRemoteRpcId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.nextRemoteRpcId();
        }
        return ROOT_RPC_ID;
    }

    @Override
    public String nextLocalRpcId() {
        String currentRpcId = getContext().rpcId;
        String nextRpcId;
        List<String> levels = Splitter.on(".").splitToList(currentRpcId);
        int size = levels.size();
        if (size == 1) {
            nextRpcId = nexLocalThreadId() + "^" + currentRpcId;
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < size - 1; i++) {
                sb.append(levels.get(i)).append(".");
            }
            sb.append(nexLocalThreadId()).append("^").append(levels.get(size - 1));
            nextRpcId = sb.toString();
        }
        return nextRpcId;
    }

    @Override
    public String getCurrentRequestId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getCurrentRequestId();
        }
        return generateRequestId(null);
    }

    public String nexLocalThreadId() {
        Context ctx = getContext();
        if (ctx != null) {
            int nexLocalThreadId = ++ctx.nexLocalThreadId;
            return nexLocalThreadId + "";
        }
        return String.valueOf(Thread.currentThread().getId());
    }

    @Override
    public String getRpcId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getRpcId();
        }
        return ROOT_RPC_ID;
    }

    @Override
    public String getCurrentRpcId() {
        Context ctx = getContext();
        if (ctx != null) {
            return ctx.getCurrentRpcId();
        }
        return ROOT_RPC_ID;
    }

    public boolean shouldLog(Throwable throwable) {
        Context ctx = context == null ? null : context.get();
        return ctx == null || ctx.shouldLog(throwable);
    }

    private void flush(String requestId, String id, Message message) {
        producer.produce(requestId, id, message);
        //reset current thread local data
        reset();
    }

    protected Context getContext() {
        Context ctx = context.get();
        if (ctx == null) {
            ctx = new Context();
            context.set(ctx);
        }
        return ctx;
    }

    /**
     * Request id must include root app id and create timestamp Format: RootAppId^^id|timestamp
     *
     * @param requestId request id
     * @return new request id
     */
    private String generateRequestId(String requestId) {
        String rid = requestId;
        if (Strings.isNullOrEmpty(requestId)) {
            rid = idFactory.getNextId();
        }
        int index = rid.indexOf("^^");
        if (index < 0) {
            int tsIndex = rid.lastIndexOf("|");
            if (tsIndex > 0) {
                return AgentConfiguration.getServiceName() + "^^" + rid;
            }
            return AgentConfiguration.getServiceName() + "^^" + rid + "|" + System.currentTimeMillis();
        } else {
            int tsIndex = rid.lastIndexOf("|");
            if (tsIndex < 0) {
                return rid + "|" + System.currentTimeMillis();
            }
        }
        return requestId;
    }

    public class Context {
        protected Stack<Transaction> stack;
        protected Set<Throwable> knownExceptions;
        /**
         * for truncate message;
         */
        protected long totalDuration;
        protected int nexLocalThreadId = 0;
        private Message root;
        private Map<String, RedisStats> redisStatsMap;
        private String redisType = null;
        private int currentCall = 0;
        private String rpcId;
        private String id;
        private String requestId;
        private int next;
        private long totalChildren = 1;
        private int redisSize = 0;
        private String clientAppId;

        /**
         * 当调用者为Trace.newTransaction,complete时,考虑以下几种情况: 1. 最简单的情况,即同步调用<code>Trace.newTransaction()</code>
         * 可以方便地和鹰眼同步的api协同 2. MultiMessageProducer时 默认关闭打鹰眼指标的功能 高级用法(不推荐)可以通过调用<code>setForceCallEagleAPI(true)
         * </code>可以开启异步模式下调用鹰眼
         * 使用方必须保证线程上下文里没有鹰眼的指标调用,否则链路层级会错乱
         */
        public Context() {
            this(null, ROOT_RPC_ID);
        }

        public Context(String requestId, String rpcId) {
            stack = new Stack<>();
            this.knownExceptions = new HashSet<>();
            setup(requestId, rpcId);

        }

        public void setup(String requestId, String rpcId) {
            this.requestId = generateRequestId(requestId);
            if (Strings.isNullOrEmpty(rpcId)) {
                rpcId = ROOT_RPC_ID;
            }
            this.id = rpcId;
            int index = rpcId.indexOf("|");
            if (index > 0) {
                this.clientAppId = rpcId.substring(0, index);
                this.rpcId = rpcId.substring(index + 1, rpcId.length());
            } else {
                this.rpcId = rpcId;
            }
        }

        public String nextRemoteRpcId() {
            completeRedis();
            currentCall++;
            return rpcId + "." + currentCall;
        }

        public String getClientAppId() {
            if (Strings.isNullOrEmpty(clientAppId)) {
                return Constants.UNKNOWN_APP_ID;
            }
            return clientAppId;
        }

        public String getCurrentRequestId() {
            return requestId;
        }

        public String getRpcId() {
            return rpcId;
        }

        public String getCurrentRpcId() {
            if (currentCall == 0) {
                return rpcId;
            }
            return rpcId + "." + currentCall;
        }

        public void mergeRedisStats(String url, String command, long duration, boolean succeed,
                                    RedisResponse[] responses, String redisType) {
            if (url == null || stack.isEmpty()) {
                return;
            }
            if (redisStatsMap == null) {
                redisStatsMap = new HashMap<>();
            }
            if (redisType != null) {
                if (this.redisType == null) {
                    this.redisType = redisType;
                } else if (!this.redisType.equals(redisType)) {
                    this.redisType = Constants.REDIS_TYPE_MIXED;
                }
            }

            RedisStats redisStats = redisStatsMap.get(url);
            if (redisStats == null) {
                if (redisSize >= configManger.getRedisSize()) {
                    throw new TooManyRedisException("Too many redis, stop create RedisStats");
                }
                redisStats = new RedisStats(url);
                redisStatsMap.put(url, redisStats);
            }
            int raiseCommand = redisStats.merge(url, command, duration, succeed, responses,
                redisSize >= configManger.getRedisSize());
            redisSize += raiseCommand;
        }

        public void completeRedis() {
            try {
                Transaction redis = resetRedis();
                if (redis == null) {
                    return;
                }
                add(redis);
            } catch (Exception e) {
            }
        }

        public void completeRedis(Transaction transaction) {
            try {
                if (transaction == null) {
                    return;
                }
                Transaction redis = resetRedis();
                if (redis == null) {
                    return;
                }
                transaction.addChild(redis);
            } catch (Exception e) {
            }
        }

        public Transaction resetRedis() {
            if (redisStatsMap == null || redisStatsMap.size() == 0) {
                return null;
            }
            Transaction redis = new TransactionImpl(Constants.REDIS_TYPE, Constants.REDIS_NAME);
            if (this.redisType == null) {
                this.redisType = Constants.REDIS_TYPE_DEFAULT;
            }
            redis.addTag(Constants.REDIS_TYPE_KEY, this.redisType);
            redis.complete();
            long duration = 0;
            for (RedisStats redisStats : redisStatsMap.values()) {
                duration += redisStats.getAllDuration();
                redis.addChild(redisStats);
            }
            redis.setDuration(duration);
            redis.setStatus(Constants.SUCCESS);
            redisStatsMap.clear();
            this.redisType = null;
            return redis;
        }

        public void add(Message message) {
            if (stack.isEmpty()) {
                //only has event in this call stack
                flush(requestId, rpcId, message);
            } else {
                //only transaction span add into call stack
                addChildren(stack.peek(), message);
            }
        }

        public boolean shouldLog(Throwable throwable) {
            if (knownExceptions == null) {
                knownExceptions = new HashSet<>();
            }
            if (knownExceptions.contains(throwable)) {
                return false;
            } else {
                knownExceptions.add(throwable);
                return true;
            }
        }

        public void start(Transaction transaction) {

            if (stack.isEmpty()) {
                root = transaction;
            } else {
                addChildren(stack.peek(), transaction);
            }

            stack.push(transaction);
        }

        public boolean end(Transaction transaction) {

            if (stack != null && !stack.isEmpty()) {
                if (transaction.isBadTransaction() && transaction != root) {
                    return false;
                }
                Transaction current = stack.pop();
                if (current != transaction) {
                    while (transaction != current && !stack.isEmpty()) {
                        if (!current.isBadTransaction()) {
                            Event event = new EventImpl(Constants.AGENT_EVENT_TYPE, Constants.NAME_BAD_TRANSACTION);
                            event.setStatus("TransactionNotCompleted");
                            event.complete();
                            current.addChild(event);
                        }
                        current = stack.pop();
                    }
                }

                if (stack.isEmpty()) {
                    if (totalDuration > 0) {
                        totalDuration = 0;
                    }
                    completeRedis(transaction);
                    // flush会reset context, 所以一些线程上下文相关的逻辑需要写在前面
                    flush(requestId, id, root);
                    return true;
                }
            }
            return false;
        }

        protected Message getRoot() {
            return root;
        }

        private void addChildren(Transaction parent, Message child) {
            if (totalChildren >= configManger.getMessageCount()) {
                truncateAndFlush((Transaction)root);
                totalChildren = 1;
                parent = (Transaction)root;
            }
            parent.addChild(child);
            totalChildren++;
        }

        private void truncateAndFlush(Transaction parent) {
            String childId = id + "~" + this.next;
            Transaction target = new TransactionImpl("Trace", Constants.NAME_TRUNCATE);
            target.setStatus(Message.SUCCESS);

            Event event = new EventImpl(Constants.AGENT_EVENT_TYPE, Constants.NAME_BAD_TRANSACTION);
            event.setStatus("TransactionLongerChildren");
            event.complete();
            target.addChild(event);

            List<Message> children = parent.getChildren();
            Iterator<Message> it = children.iterator();
            List<Message> truncateEvents = new ArrayList<>();
            while (it.hasNext()) {
                Message message = it.next();
                if (!(Constants.TYPE_ETRACE_LINK.equals(message.getType()) && Constants.NAME_TRUNCATE.equals(
                    message.getName()))) {
                    if (!message.isCompleted()) {
                        message.complete();
                    }
                    target.addChild(message);
                } else {
                    truncateEvents.add(message);
                }
            }

            if (truncateEvents.size() > 100) {
                for (Message message : truncateEvents) {
                    target.addChild(message);
                }
                truncateEvents.clear();
            }

            this.next++;
            totalDuration = 1;//totalDuration + parent.getDuration();

            flush(getCurrentRequestId(), childId, target);
            Event next = new EventImpl(Constants.TYPE_ETRACE_LINK, Constants.NAME_TRUNCATE);
            next.setData(childId);
            next.setStatus(Message.SUCCESS);
            parent.setChildren(truncateEvents);
            parent.addChild(next);
        }
    }
}