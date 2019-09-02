package io.etrace.common.modal;

import java.util.Map;
import java.util.Objects;

public class EsightData {

    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String InsertWithUpdateIfPresent = "insertWithUpdate";

    private String tableName;

    private Map<String, Object> data;
    /**
     * 'insert' or 'update' or 'insertWithUpdate': the action of this data
     */
    private String type;
    private EsightUpdateData updateData;
    private long chkPresentDuration;

    public EsightData() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EsightUpdateData getUpdateData() {
        return updateData;
    }

    public void setUpdateData(EsightUpdateData updateData) {
        this.updateData = updateData;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public long getChkPresentDuration() {
        return chkPresentDuration;
    }

    public void setChkPresentDuration(long chkPresentDuration) {
        this.chkPresentDuration = chkPresentDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        EsightData that = (EsightData)o;
        return chkPresentDuration == that.chkPresentDuration &&
            Objects.equals(tableName, that.tableName) &&
            Objects.equals(data, that.data) &&
            Objects.equals(type, that.type) &&
            Objects.equals(updateData, that.updateData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, data, type, updateData, chkPresentDuration);
    }
}
