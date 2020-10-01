package io.etrace.stream.biz.app.event;

public class JVMMemoryPool extends AbstractJVM {
    private String subType;

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }
}