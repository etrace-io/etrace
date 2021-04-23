package io.etrace.api.model.vo.ui;

import lombok.Data;

import java.util.List;

@Data
public class DashboardApp {
    private Boolean critical;
    private List<Long> dashboardIds;
    private List<Dashboard> dashboards;
    private String title;
    private String description;
    private String status;
    private Long favoriteCount;
    private String createdBy;
    private String updatedBy;
}
