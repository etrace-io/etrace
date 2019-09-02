package io.etrace.common.modal;

import java.util.Map;

public class EsightUpdateData {
    private Map<String, Object> data;
    private Map<String, Object> condition;

    public EsightUpdateData() {
    }

    public EsightUpdateData(Map<String, Object> data, Map<String, Object> condition) {
        this.data = data;
        this.condition = condition;
    }

    public boolean validate() {
        return data != null && data.size() > 0 && condition != null && condition.size() > 0;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getCondition() {
        return condition;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        EsightUpdateData that = (EsightUpdateData)o;

        if (data != null ? !data.equals(that.data) : that.data != null) { return false; }
        return condition != null ? condition.equals(that.condition) : that.condition == null;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        return result;
    }
}
