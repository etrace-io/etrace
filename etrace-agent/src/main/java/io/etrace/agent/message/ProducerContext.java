package io.etrace.agent.message;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.etrace.agent.config.AgentConfiguration;
import io.etrace.agent.config.DiskFileConfiguration;
import io.etrace.common.util.NetworkInterfaceHelper;

public class ProducerContext<E> {
    private volatile boolean active = true;
    private String hostIp;
    private String hostName;
    private String idc;
    private String cluster;
    private String ezone;
    private String mesosTaskId;
    private String eleapposLabel;
    private String eleapposSlaveFqdn;
    private String instance;
    private DiskFileConfiguration diskFileConfiguration;
    private Disruptor<E> disruptor;
    private RingBuffer<E> ringBuffer;

    public ProducerContext() {
        hostIp = NetworkInterfaceHelper.INSTANCE.getLocalHostAddress();
        hostName = NetworkInterfaceHelper.INSTANCE.getLocalHostName();
        diskFileConfiguration = AgentConfiguration.getDiskFileConfiguration();
        idc = diskFileConfiguration.getIdc();
        cluster = diskFileConfiguration.getCluster();
        ezone = diskFileConfiguration.getEzone();
        mesosTaskId = AgentConfiguration.getMesosTaskId();
        eleapposLabel = AgentConfiguration.getEleapposLabel();
        eleapposSlaveFqdn = AgentConfiguration.getEleapposSlaveFqdn();
        instance = AgentConfiguration.getInstance();
    }

    public void build(String name, int bufferSize, final EventHandler handler, EventFactory<E> factory) {

        // Construct the Disruptor
        disruptor = new Disruptor<E>(factory, bufferSize, r -> {
            Thread t = new Thread(r);
            t.setName(name);
            t.setDaemon(true);
            return t;
        }, ProducerType.MULTI, new LiteBlockingWaitStrategy());
        disruptor.handleEventsWith(handler);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<E>() {
            @Override
            public void handleEventException(Throwable ex, long sequence, E event) {

            }

            @Override
            public void handleOnStartException(Throwable ex) {

            }

            @Override
            public void handleOnShutdownException(Throwable ex) {

            }
        });
        ringBuffer = disruptor.getRingBuffer();
        disruptor.start();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getInstance() {
        return instance;
    }

    public String getHostIp() {
        return hostIp;
    }

    public String getHostName() {
        return hostName;
    }

    public String getIdc() {
        return idc;
    }

    public String getCluster() {
        return cluster;
    }

    public String getEzone() {
        return ezone;
    }

    public String getMesosTaskId() {
        return mesosTaskId;
    }

    public String getEleapposLabel() {
        return eleapposLabel;
    }

    public String getEleapposSlaveFqdn() {
        return eleapposSlaveFqdn;
    }

    public DiskFileConfiguration getDiskFileConfiguration() {
        return diskFileConfiguration;
    }

    public Disruptor<E> getDisruptor() {
        return disruptor;
    }

    public RingBuffer<E> getRingBuffer() {
        return ringBuffer;
    }

    public int getQueueSize() {
        return ringBuffer.getBufferSize() - (int)ringBuffer.remainingCapacity();
    }

}
