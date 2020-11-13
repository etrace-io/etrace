package io.etrace.api.model.po.yellowpage;

import io.etrace.api.model.po.BasePersistentObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

@Data
@Entity(name = "search_list_record_mapping")
@EqualsAndHashCode(callSuper = true)
public class SearchListRecordMapping extends BasePersistentObject {

    private Long listId;

    private Long recordId;

    private String status;

}
