package io.etrace.api.model.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode
public class BaseObject implements Serializable {

    @Id
    private Long id;

    private Date updatedAt;

    private Date createdAt;
}
