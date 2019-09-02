package io.etrace.common.modal;

public interface Event extends Message {
    String getData();

    void setData(String data);
}
