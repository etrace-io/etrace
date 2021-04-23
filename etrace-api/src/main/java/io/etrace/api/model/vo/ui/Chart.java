package io.etrace.api.model.vo.ui;

import io.etrace.api.model.Target;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;

import javax.annotation.Nullable;
import javax.persistence.Convert;
import java.util.List;

@Data
public class Chart {
    private String config;
    private List<Target> targets;
    private String title;
    private String description;
    private String status;
    private String createdBy;
    private String updatedBy;
}
