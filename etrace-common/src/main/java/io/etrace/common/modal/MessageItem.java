package io.etrace.common.modal;

import com.google.common.base.Strings;
import io.etrace.common.util.RequestIdHelper;

public class MessageItem {
    private CallStack callStack;
    private long blockId;
    private int offset;
    private String dataFile;
    private String requestId;
    private byte[] messageData;

    private long hour;
    private long ip;
    private int index;

    private boolean isDal;

    public MessageItem(CallStack callStack) {
        this(callStack, null);
    }

    public MessageItem(CallStack callStack, MessageHeader messageHeader) {
        this.callStack = callStack;
        if (callStack != null) {
            this.requestId = RequestIdHelper.removeRootAppId(callStack.getRequestId());
        }
    }

    public long getBlockId() {
        return blockId;
    }

    public void setBlockId(long blockId) {
        this.blockId = blockId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getMessageData() {
        return messageData;
    }

    public void setMessageData(byte[] messageData) {
        this.messageData = messageData;
    }

    public CallStack getCallStack() {
        return callStack;
    }

    public String getDataFile() {
        return dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        if (!Strings.isNullOrEmpty(requestId)) {
            this.requestId = RequestIdHelper.removeRootAppId(callStack.getRequestId());
        }
    }

    public String getSampleMessageId() {
        return requestId + "$$" + callStack.getId();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getHour() {
        return hour;
    }

    public void setHour(long hour) {
        this.hour = hour;
    }

    public long getIp() {
        return ip;
    }

    public void setIp(long ip) {
        this.ip = ip;
    }

    public boolean isDal() {
        return isDal;
    }

    public void setIsDal(boolean isDal) {
        this.isDal = isDal;
    }
}
