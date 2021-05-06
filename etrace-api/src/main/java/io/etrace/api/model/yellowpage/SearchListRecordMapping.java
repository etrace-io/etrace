package io.etrace.api.model.yellowpage;

import lombok.Getter;
import lombok.Setter;
import me.ele.arch.monitor.api.model.BaseModel;

/**
 * @author chunle.pei
 * @date 2020/3/5
 */
@Getter
@Setter
public class SearchListRecordMapping extends BaseModel {

    private Long listId;

    private Long recordId;

    private String status;

}
