package io.etrace.common.util;

import io.etrace.common.exception.ShutdownHookException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownHookManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(ShutdownHookManager.class);
    private static final ShutdownHookManager MGR = new ShutdownHookManager();
    private final Set<HookEntry> hooks =
        Collections.synchronizedSet(new HashSet<>());
    private AtomicBoolean shutdownInProgress = new AtomicBoolean(false);
    private static int hookExecutionCounter = 1;

    //private to constructor to ensure singularity
    private ShutdownHookManager() {
    }

    static {
        Runtime.getRuntime().addShutdownHook(
            new Thread() {
                @Override
                public void run() {
                    MGR.shutdownInProgress.set(true);
                    List<Runnable> shutdownHooks = MGR.getShutdownHooksInOrder();
                    beforeExecuteShutdownHooks(shutdownHooks);
                    try {
                        for (Runnable hook : shutdownHooks) {
                            try {
                                executeShutDownHook(hook);
                            } catch (Throwable ex) {
                                System.out.println(
                                    "ShutdownHook '" + hook.getClass().getSimpleName() + "' failed, " + ex.toString());
                                ex.printStackTrace();
                            }
                        }
                    } finally {
                        System.out.println("All shutdown hook execution finished.");
                    }
                }
            }
        );
    }

    private static void executeShutDownHook(Runnable runnable) {
        Date startTime = new Date();
        System.out.println(hookExecutionCounter + ":Executing " + runnable + " at " + dateString(startTime));
        runnable.run();
        Date endTime = new Date();
        System.out.println(hookExecutionCounter + ":Finished " + runnable + " at " + dateString(endTime) + ". " +
            "Takes " + (endTime.getTime() - startTime.getTime()) + "ms");
        hookExecutionCounter++;
    }

    private static String dateString(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return fmt.format(date);
    }

    private static void beforeExecuteShutdownHooks(List<Runnable> shutdownHooks) {
        System.out.println("Total hooks = " + shutdownHooks.size());
        shutdownHooks.forEach(System.out::println);
        System.out.println("Executing shutdown hooks...");
    }

    /**
     * Return ShutdownHookManager singleton.
     *
     * @return ShutdownHookManager singleton.
     */
    public static ShutdownHookManager get() {
        return MGR;
    }

    public List<Runnable> getShutdownHooksInOrder() {
        List<HookEntry> list;
        synchronized (MGR.hooks) {
            list = new ArrayList<>(MGR.hooks);
        }
        Collections.sort(list, (o1, o2) -> o2.priority.getPriority() - o1.priority.getPriority());
        List<Runnable> ordered = new ArrayList<>();
        list.forEach((entry) -> ordered.add(entry.hook));
        return ordered;
    }

    public void addShutdownHook(Runnable shutdownHook, int priority) {
        addShutdownHook(shutdownHook, new Priority(priority));
    }

    private void addShutdownHook(Runnable shutdownHook, Priority priority) {
        if (shutdownHook == null) {
            throw new ShutdownHookException("shutdownHook cannot be NULL");
        }
        if (shutdownInProgress.get()) {
            throw new ShutdownHookException("Shutdown in progress, cannot add a shutdownHook");
        }
        hooks.add(new HookEntry(shutdownHook, priority));
    }

    public boolean removeShutdownHook(Runnable shutdownHook) {
        if (shutdownInProgress.get()) {
            throw new IllegalStateException("Shutdown in progress, cannot remove a shutdownHook");
        }
        return hooks.remove(new HookEntry(shutdownHook, Priority.HIGH));
    }

    /**
     * Private structure to store ShutdownHook and its priority.
     */
    private static class HookEntry {
        Runnable hook;
        Priority priority;

        public HookEntry(Runnable hook, Priority priority) {
            this.hook = hook;
            this.priority = priority;
        }

        @Override
        public int hashCode() {
            return hook.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean eq = false;
            if (obj != null) {
                if (obj instanceof HookEntry) {
                    eq = (hook == ((HookEntry)obj).hook);
                }
            }
            return eq;
        }
    }

    public static class Priority {
        public static Priority HIGH = new Priority(99);
        public static Priority LOW = new Priority(1);

        private int priority;

        Priority(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}
