package io.etrace.api.model;

import lombok.Data;

@Data
public class ExceptionEntity {

    private Integer status;
    private String reasonPhrase;
    private String message;

}
