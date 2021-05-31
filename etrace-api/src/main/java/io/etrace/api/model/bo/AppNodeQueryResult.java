package io.etrace.api.model.bo;

import lombok.Data;

@Data
public class AppNodeQueryResult extends SimpleNodeQueryResult {

    private String appId;
    private int publish;
    private int alert;
    private int change;
}
