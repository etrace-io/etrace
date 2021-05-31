package io.etrace.api.model.vo;

import io.etrace.common.constant.Status;
import io.etrace.common.datasource.OneDatasourceConfig;
import lombok.Data;

import java.util.List;

@Data
public class DataSource {

    private String name;
    private String type;

    private List<OneDatasourceConfig> config;

    private Status status = Status.Active;
}
