package io.etrace.api.model.po.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class SearchKeyWord extends BasePersistentObject {

    /**
     * 关键字
     */
    private String name;

    /**
     * 状态
     */
    private String status;
}
