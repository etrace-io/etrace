package io.etrace.api.model.po.user;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

@Data
@Entity(name = "user")
public class ETraceUserPO extends BasePersistentObject {

    /**
     * 可重复（不过应尽量避免）
     */
    private String userName;
    private String password;

    @Column(unique = true, nullable = false)
    private String email;
}
