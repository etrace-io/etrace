package io.etrace.api.model.vo.ui;

import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.ui.DashboardPO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import javax.annotation.Nullable;
import java.util.List;

@Data
public class DashboardVO extends BaseItem {

    private String layout;
    private String config;

    private List<Long> chartIds;

    private List<ChartVO> charts;

    @Nullable
    private String title;
    private String description;
    private String status;
    private Long favoriteCount;

    public static DashboardVO toVO(DashboardPO po) {
        DashboardVO vo = new DashboardVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public DashboardPO toPO() {
        DashboardPO po = new DashboardPO();
        BeanUtils.copyProperties(this, po);
        return po;
    }
}
