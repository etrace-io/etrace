package io.etrace.api.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationInfo {
    private Long created_atd;

    private String aone_name;

    private String app_type;

    private String appid;
    private List<EappAppMember> appid_members;

    private String category;
    private Integer cost_parent_id;

    private String cost_parent_name;

    private Long created_at;
    private Boolean critical;

    private Boolean global_zone;

    private String module_name;
    private Boolean multi_zone;

    private Boolean on_transac_path;

    //所属父组织结构id
    private Long department_parent_id;
    //所属父组织结构名称
    private String department_parent_name;
    //所属子组织结构id
    private Long department_child_id;
    //所属子组织结构名称
    private String department_child_name;
    //所属父产线id
    private Long product_line_parent_id;
    //所属父产线构名称
    private String product_line_parent_name;
    //所属子产线构id
    private Long product_line_child_id;
    //所属子产线构名称
    private String product_line_child_name;
}
