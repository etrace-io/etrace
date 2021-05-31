package io.etrace.api.model.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;

/**
 * 记录和关键字关系映射表
 */
@Data
public class SearchRecordKeyWordMapping extends BasePersistentObject {

    private long recordId;

    private long keywordId;

    private String status;
}
