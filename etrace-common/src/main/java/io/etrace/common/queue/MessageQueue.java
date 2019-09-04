package io.etrace.common.queue;

public interface MessageQueue<M> {
    boolean offer(M message);

    M poll();

    /**
     * Return size of the current queue
     *
     * @return
     */
    int size();
}
