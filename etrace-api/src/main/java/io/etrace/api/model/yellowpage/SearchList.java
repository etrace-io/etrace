package io.etrace.api.model.yellowpage;

import io.etrace.api.model.SearchListTypeEnum;
import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;

@Data
public class SearchList extends BasePersistentObject {

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
     *
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
