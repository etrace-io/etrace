package io.etrace.api.model.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;

@Data
public class SearchListRecordMapping extends BasePersistentObject {

    private Long listId;

    private Long recordId;

    private String status;

}
