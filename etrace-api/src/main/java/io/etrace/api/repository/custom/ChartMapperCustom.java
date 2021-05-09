package io.etrace.api.repository.custom;

import io.etrace.api.model.vo.ui.ChartVO;

import java.util.List;

public interface ChartMapperCustom {

    int count(String title, String globalId, Long departmentId, Long productLineId, String user, String status);

    List<ChartVO> search(String title, String globalId, Long departmentId, Long productLineId, String user,
                         int start, int pageSize, String status);
}
