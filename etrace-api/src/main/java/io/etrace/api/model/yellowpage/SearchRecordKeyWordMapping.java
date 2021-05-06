package io.etrace.api.model.yellowpage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.ele.arch.monitor.api.model.BaseModel;

/**
 * 记录和关键字关系映射表
 *
 * @author chunle.pei
 * @date 2020/1/6
 */
@Getter
@Setter
@ToString
public class SearchRecordKeyWordMapping extends BaseModel {

    private long recordId;

    private long keywordId;

    private String status;
}
