package io.etrace.api.model.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;

/**
 * 关联性model
 */
@Data
public class SearchKeyWordCorrelation extends BasePersistentObject {

    private Long keywordId;

    /**
     * 相关联的keywordId
     */
    private Long correlationKeywordId;
    /**
     * 相关性 0-100 0代表不相关，100代表完全相关（即a关键字出现时候b关键字一定出现，反之未必）
     */
    private int correlationCefficient;
}
