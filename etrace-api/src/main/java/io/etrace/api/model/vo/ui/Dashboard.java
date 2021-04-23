package io.etrace.api.model.vo.ui;

import lombok.Data;

import javax.annotation.Nullable;
import java.util.List;

@Data
public class Dashboard {

    private String layout;
    private String config;

    private List<Long> chartIds;

    private List<Chart> charts;


    @Nullable
    private String title;
    private String description;
    private String status;
    private Long favoriteCount;
    @Nullable
    private String createdBy;
    @Nullable
    private String updatedBy;
}
