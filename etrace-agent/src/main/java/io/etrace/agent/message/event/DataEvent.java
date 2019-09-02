package io.etrace.agent.message.event;

import com.lmax.disruptor.EventFactory;

public class DataEvent {
    private byte[] buffer;
    private String key;
    private int count;

    public byte[] getBuffer() {
        return buffer;
    }

    public int getCount() {
        return count;
    }

    public String getKey() {
        return key;
    }

    public void reset(byte[] data, int count, String key) {
        this.buffer = data;
        this.count = count;
        this.key = key;
    }

    public void clear() {
        this.buffer = null;
        this.count = 0;
        this.key = null;
    }

    public static class DataEventFactory implements EventFactory<DataEvent> {

        @Override
        public DataEvent newInstance() {
            return new DataEvent();
        }
    }
}
