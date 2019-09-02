package io.etrace.agent.config;

import io.etrace.common.message.ConfigManger;
import io.etrace.common.modal.Collector;
import io.etrace.common.modal.CollectorItem;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

public class CollectorRegistry {
    private final CollectorItem collectorItem = new CollectorItem(10);
    private final Pattern IP_PATTERN = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private AtomicLong collectorIndex = new AtomicLong(0);
    private boolean longConnection = true;
    private volatile boolean isAvailable = true;
    private volatile boolean useTcp = false;
    private volatile boolean isInit = false;
    private ConfigManger configManger;

    public static CollectorRegistry getInstance() {
        return CollectorRegistryHolder.instance;
    }

    public void setCollectorItem(CollectorItem item) {
        if (null == item || collectorItem.equals(item)) {
            return;
        }
        isInit = true;
        collectorItem.setUseTcp(item.isUseTcp());
        useTcp = collectorItem.isUseTcp();
        item.getTcpCollector().forEach(collector -> addCollector(collector, collectorItem.getTcpCollector()));
        item.getThriftCollector().forEach(collector -> addCollector(collector, collectorItem.getThriftCollector()));

        collectorItem.getTcpCollector().retainAll(item.getTcpCollector());
        collectorItem.getThriftCollector().retainAll(item.getThriftCollector());

        Collections.shuffle(collectorItem.getTcpCollector());
        Collections.shuffle(collectorItem.getThriftCollector());
    }

    private void addCollector(Collector collector, List<Collector> collectorList) {
        try {
            if (null == collector || null == collector.getIp() || !IP_PATTERN.matcher(collector.getIp()).matches() ||
                collector.getPort() < 1 || collectorList.contains(collector)) {
                return;
            }
            collectorList.add(collector);
        } catch (Exception e) {
            //ignore
        }
    }

    public boolean isLongConnection() {
        return longConnection;
    }

    public void setLongConnection(boolean longConnection) {
        this.longConnection = longConnection;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setIsAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    private void init() {
        if (!isInit) {
            configManger.init();
        }
    }

    public int getTcpCollectorSize() {
        init();
        return collectorItem.getTcpCollector().size();
    }

    public int getCollectorSize() {
        init();
        return collectorItem.getThriftCollector().size();
    }

    public Collector getTcpCollector() {
        if (collectorItem.getTcpCollector().isEmpty()) {
            return null;
        }
        long next = collectorIndex.getAndAdd(1) % collectorItem.getTcpCollector().size();
        return collectorItem.getTcpCollector().get((int)next);
    }

    public Collector getThriftCollector() {
        if (collectorItem.getThriftCollector().isEmpty()) {
            return null;
        }
        long next = collectorIndex.getAndAdd(1) % collectorItem.getThriftCollector().size();
        return collectorItem.getThriftCollector().get((int)next);
    }

    public int getCollectorsSize() {
        if (useTcp) {
            return collectorItem.getTcpCollector().size();
        }
        return collectorItem.getThriftCollector().size();
    }

    public boolean isUseTcp() {
        if (!isInit) {
            configManger.init();
        }
        return useTcp;
    }

    public void clearCollectors() {
        try {
            collectorItem.getTcpCollector().clear();
        } catch (Exception e) {
            //ignore
        }
        try {
            collectorItem.getThriftCollector().clear();
        } catch (Exception e) {
            //ignore
        }
    }

    public void setConfigManger(ConfigManger configManger) {
        this.configManger = configManger;
    }

    private static class CollectorRegistryHolder {
        private static CollectorRegistry instance = new CollectorRegistry();
    }
}
