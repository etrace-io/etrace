package io.etrace.api.model.po.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * 关联性model
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class SearchKeyWordCorrelation extends BasePersistentObject {

    private Long keywordId;

    /**
     * 相关联的keywordId
     */
    private Long correlationKeywordId;
    /**
     * 相关性 0-100 0代表不相关，100代表完全相关（即a关键字出现时候b关键字一定出现，反之未必） correlation Coefficient
     */
    private int correlationCoefficient;
}
