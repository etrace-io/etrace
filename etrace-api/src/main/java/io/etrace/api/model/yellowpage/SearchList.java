package io.etrace.api.model.yellowpage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.ele.arch.monitor.api.consts.SearchListTypeEnum;
import me.ele.arch.monitor.api.model.BaseModel;
import me.ele.arch.monitor.api.model.Status;

/**
 * @author chunle.pei
 * @date 2019/12/31
 */
@Getter
@Setter
@ToString
public class SearchList extends BaseModel {

    /**
     * 名称
     */
    private String name;

    /**
     * icon
     */
    private String icon;
    /**
     * 状态 {@link Status}
     */
    private String status;

    /**
     * 类型 {@link me.ele.arch.monitor.api.consts.SearchListTypeEnum}
     */
    private Integer listType = SearchListTypeEnum.NEWEST.getCode();
    /**
     * 维护者的阿里邮箱
     */
    private String maintainerAliEmail;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 修改人
     */
    private String updatedBy;

    /**
     * 下面是数据库的extend 字段
     */

    private Boolean star = false;
}
