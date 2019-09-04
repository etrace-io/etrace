package io.etrace.collector.route.worker;

import io.etrace.common.modal.MessageHeader;
import io.etrace.common.modal.Pair;
import io.etrace.common.queue.PersistentQueue;

/**
 * @author jie.huang
 *         Date: 2018/5/10
 *         Time: 下午11:06
 */
public interface Worker extends Runnable {

    void init(PersistentQueue<Pair<MessageHeader, byte[]>> queue);

    void startup();

    boolean produce(Pair<MessageHeader, byte[]> msg);

    void shutdown();

    void checkOverflow();
}
