package io.etrace.api.model.po.ui;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@Entity(name = "graph")
@EqualsAndHashCode(callSuper = true)
public class SearchKeyWord extends BasePersistentObject {

    /**
     * 关键字
     */
    private String name;

    private String status;
}
