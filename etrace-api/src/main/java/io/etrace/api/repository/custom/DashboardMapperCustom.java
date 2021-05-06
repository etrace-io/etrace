package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.Dashboard;
import io.etrace.api.model.vo.Department;

import java.util.List;

public interface DashboardMapperCustom {

    int count(String title, Long departmentId, Long productLineId, String globalId, String user,
              String status);

    List<Dashboard> search(String title, Long departmentId, Long productLineId, String globalId, String user,
                           String status, int start, int pageSize);

    List<Dashboard> findByIds(String title, Long departmentId, Long productLineId, List<Long> dashboardIds);

    int countDashboardCountByDepartment(Long departmentId, String status, String user, List<Long> dashboardIdList);

    int countDashboardCountByProductline(Long productLineId, String status, String user, List<Long> dashboardIdList);

    List<Department> findDashboardGroupByDepartment(String status, String user, List<Long> dashboardIdList);

    List<Department> findDashboardGroupByProductLine(Long departmentId, String status, String user,
                                                     List<Long> dashboardIdList);

}
