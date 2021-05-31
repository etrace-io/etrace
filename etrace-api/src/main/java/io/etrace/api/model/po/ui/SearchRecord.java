package io.etrace.api.model.po.ui;

import io.etrace.api.model.SearchRecordTypeEnum;
import io.etrace.api.model.po.BasePersistentObject;
import io.etrace.api.util.JpaConverterJson;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Convert;
import javax.persistence.Entity;
import java.util.List;

@Data
@Entity(name = "search_record")
@EqualsAndHashCode(callSuper = true)
public class SearchRecord extends BasePersistentObject {

    private String name;

    private Integer type;

    private String status;
    /**
     * 记录的地址
     */
    private String url;
    /**
     * 文件记录id
     */
    private Long fileRecordId;
    /**
     * 点击数
     */
    private int clickIndex;
    /**
     * 收藏数
     */
    private int favoriteIndex;
    /**
     * owner的阿里邮箱
     */
    private String ownerAliEmail;
    /**
     * 钉钉群号
     */
    private String dingtalkNumber;

    /**
     * 维护者的钉钉号，便于前端直接拉起来
     */
    private String ownerDingtalkNumber;

    /**
     * 钉钉名
     */
    private String ownerDingtalkame;

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

    // 下面为拓展字段

    /**
     * 文件下载地址
     */
    private String icon;

    /**
     * 关键字列表
     */
    @Convert(converter = JpaConverterJson.class)
    private List<SearchKeyWord> keywordList;

    private Boolean star;

    private String typeName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        SearchRecordTypeEnum recordTypeEnum = SearchRecordTypeEnum.ofName(this.typeName);
        if (null != recordTypeEnum) {
            return recordTypeEnum.getCode();
        }
        return type;
    }

    public String getTypeName() {
        if (null != typeName) {
            return typeName;
        }
        SearchRecordTypeEnum recordTypeEnum = SearchRecordTypeEnum.ofCode(type);
        if (null != recordTypeEnum) {
            return recordTypeEnum.name();
        }
        return typeName;
    }

}
