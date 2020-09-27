/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.common.message.metric.field;

public class Field {

    private AggregateType aggregateType;
    private double value;

    public Field() {
    }

    public Field(AggregateType aggregateType, double value) {
        this.aggregateType = aggregateType;
        this.value = value;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void merge(Field otherField) {
        switch (aggregateType) {
            case SUM:
                value += otherField.value;
                break;
            case GAUGE:
                value = otherField.value;
                break;
            case MIN:
                if (value > otherField.value) {
                    value = otherField.value;
                }
                break;
            case MAX:
                if (value < otherField.value) {
                    value = otherField.value;
                }
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Field)) { return false; }
        Field field = (Field)o;
        return value == field.value &&
            aggregateType == field.aggregateType;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = aggregateType != null ? aggregateType.hashCode() : 0;
        temp = Double.doubleToLongBits(value);
        result = 31 * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Field{" +
            "aggregateType=" + aggregateType +
            ", value=" + value +
            '}';
    }
}
