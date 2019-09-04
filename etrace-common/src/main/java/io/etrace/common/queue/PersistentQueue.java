package io.etrace.common.queue;

public interface PersistentQueue<T> {

    boolean produce(T data);

    T consume();

    void setQueueCodec(QueueCodec codec);

    long remainingCapacity();

    long capacity();

    long usedSize();

    boolean isEmpty();

    void shutdown();

    /**
     * Memory overflow count
     *
     * @return overflow count
     */
    long getOverflowCount();

    /**
     * back file size in MB
     *
     * @return back file size
     */
    int getBackFileSize();
}
