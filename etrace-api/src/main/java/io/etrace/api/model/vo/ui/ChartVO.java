package io.etrace.api.model.vo.ui;

import io.etrace.api.model.Target;
import io.etrace.api.model.po.BaseItem;
import io.etrace.api.model.po.ui.ChartPO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

@Data
public class ChartVO extends BaseItem {
    private String config;
    private List<Target> targets;
    private String title;
    private String description;
    private String status;

    public static ChartVO toVO(ChartPO po) {
        ChartVO vo = new ChartVO();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public ChartPO toPO() {
        ChartPO po = new ChartPO();
        BeanUtils.copyProperties(this, po);
        return po;
    }
}
