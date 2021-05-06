package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.Chart;
import io.etrace.api.model.vo.Department;

import java.util.List;

public interface ChartMapperCustom {

    int count(String title, String globalId, Long departmentId, Long productLineId, String user, String status);

    List<Chart> search(String title, String globalId, Long departmentId, Long productLineId, String user,
                       int start, int pageSize, String status);

    int countChartCountByDepartment(Long departmentId, String status, String user);

    int countChartCountByProductline(Long productLineId, String status, String user);

    List<Department> findChartGroupByDepartment(String status, String user);

    List<Department> findChartGroupByProductLine(Long departmentId, String status, String user);
}
