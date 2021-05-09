package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.ui.DashboardVO;

import java.util.List;

public interface DashboardMapperCustom {

    int count(String title, Long departmentId, Long productLineId, String globalId, String user,
              String status);

    List<DashboardVO> search(String title, Long departmentId, Long productLineId, String globalId, String user,
                             String status, int start, int pageSize);

    List<DashboardVO> findByIds(String title, Long departmentId, Long productLineId, List<Long> dashboardIds);
}
