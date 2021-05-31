package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.DataSource;

import java.util.List;

public interface DataSourceMapperCustom {
    int count(String type, String name, String status);

    List<DataSource> search(String type, String name, String status, int start, int pageSize);
}
