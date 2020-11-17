package io.etrace.api.service;

import io.etrace.api.model.po.ui.MetricDataSource;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.DataSourceMapper;
import io.etrace.common.constant.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataSourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);

    @Autowired
    private DataSourceMapper dataSourceMapper;

    public Long create(MetricDataSource datasource) {
        return dataSourceMapper.save(datasource).getId();
    }

    public Optional<MetricDataSource> findById(long id) {
        return dataSourceMapper.findById(id);
    }

    public Iterable<MetricDataSource> findAll() {
        return dataSourceMapper.findAll();
    }

    public SearchResult<MetricDataSource> search(String type, String name, String status, Integer pageSize,
                                                 Integer pageNum) {
        SearchResult<MetricDataSource> searchResult = new SearchResult<>();
        int count = dataSourceMapper.countByTypeAndNameAndStatus(type, name, status);
        searchResult.setTotal(count);
        Integer start = (pageNum - 1) * pageSize;

        PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
        List<MetricDataSource> metricDataSources = dataSourceMapper.findAllByTypeAndNameAndStatus(type, name, status,
            pageRequest);
        searchResult.setResults(metricDataSources);
        return searchResult;
    }

    public void update(MetricDataSource datasource) {
        dataSourceMapper.save(datasource);
    }

    public void updateStatus(long id, String status) {
        dataSourceMapper.updateStatus(id, status);
        MetricDataSource metricDataSource = new MetricDataSource();
        metricDataSource.setId(id);
        Status statusEnum = Status.forName(status);
        if (null != statusEnum) {
            metricDataSource.setStatus(statusEnum);
        }
    }
}
