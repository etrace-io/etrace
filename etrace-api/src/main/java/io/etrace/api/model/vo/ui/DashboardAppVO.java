package io.etrace.api.model.vo.ui;

import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.ui.DashboardAppPO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class DashboardAppVO extends BaseItem {
    private Boolean critical;
    private List<Long> dashboardIds;
    private List<DashboardVO> dashboards;
    private String title;
    private String description;
    private String status;
    private Long favoriteCount;

    public static DashboardAppVO toVO(DashboardAppPO po) {
        DashboardAppVO vo = new DashboardAppVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public DashboardAppPO toPO() {
        DashboardAppPO po = new DashboardAppPO();
        BeanUtils.copyProperties(this, po);
        return po;
    }
}
