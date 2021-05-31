//package io.etrace.api.repository.custom;
//
//import io.etrace.api.model.vo.ui.DashboardAppVO;
//
//import java.util.List;
//
//public interface DashboardAppMapperCustom {
//
//    List<DashboardAppVO> findByIds(String title, List<Long> ids);
//
//    int count(String title, Long departmentId, Long productLineId, String user, Boolean critical);
//
//    List<DashboardAppVO> search(String title, Long departmentId, Long productLineId, String user, Boolean critical,
//                                int start, int pageSize);
//
//    List<DashboardAppVO> settingSearch(String title, Boolean critical);
//
//    int countDashboardAppCountByDepartment(Long departmentId, String status, String user,
//                                           List<Long> dashboardAppIdList);
//
//    int countDashboardAppCountByProductline(Long productLineId, String status, String user,
//                                            List<Long> dashboardAppIdList);
//}
