package io.etrace.agent.message.event;

import com.lmax.disruptor.EventFactory;
import io.etrace.common.modal.CallStack;
import io.etrace.common.modal.Message;

public class MessageEvent {
    private CallStack callStack = new CallStack();

    public CallStack getCallStack() {
        return callStack;
    }

    public void reset(String appId, String hostIp, String hostName, String requestId, String messageId, Message message,
                      String cluster, String ezone, String idc, String mesosTaskId, String eleapposLabel,
                      String eleapposSlaveFqdn, String instance) {
        callStack.setAppId(appId);
        callStack.setHostIp(hostIp);
        callStack.setHostName(hostName);
        callStack.setRequestId(requestId);
        callStack.setId(messageId);
        callStack.setMessage(message);
        callStack.setCluster(cluster);
        callStack.setEzone(ezone);
        callStack.setIdc(idc);
        callStack.setMesosTaskId(mesosTaskId);
        callStack.setEleapposLabel(eleapposLabel);
        callStack.setEleapposSlaveFqdn(eleapposSlaveFqdn);
        callStack.setInstance(instance);
    }

    public void clear() {
        this.callStack.clear();
    }

    public static class MessageEventFactory implements EventFactory<MessageEvent> {
        @Override
        public MessageEvent newInstance() {
            return new MessageEvent();
        }
    }
}
