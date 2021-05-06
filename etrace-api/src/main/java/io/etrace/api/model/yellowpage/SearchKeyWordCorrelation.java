package io.etrace.api.model.yellowpage;

import lombok.Getter;
import lombok.Setter;
import me.ele.arch.monitor.api.model.BaseModel;

/**
 * 关联性model
 */
@Getter
@Setter
public class SearchKeyWordCorrelation extends BaseModel {

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
