package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.DashboardApp;
import io.etrace.api.model.vo.Department;

import java.util.List;

public interface DashboardAppMapperCustom {

    List<DashboardApp> findByIds(String title, List<Long> ids);

    int count(String title, Long departmentId, Long productLineId, String user, Boolean critical);

    List<DashboardApp> search(String title, Long departmentId, Long productLineId, String user, Boolean critical,
                              int start, int pageSize);

    List<DashboardApp> settingSearch(String title, Boolean critical);

    int countDashboardAppCountByDepartment(Long departmentId, String status, String user,
                                           List<Long> dashboardAppIdList);

    int countDashboardAppCountByProductline(Long productLineId, String status, String user,
                                            List<Long> dashboardAppIdList);

    List<Department> findDashboardAppGroupByDepartment(String status, String user, List<Long> favoriteApps);

    List<Department> findDashboardAppGroupByProductLine(Long departmentId, String status, String user,
                                                        List<Long> favoriteApps);
}
