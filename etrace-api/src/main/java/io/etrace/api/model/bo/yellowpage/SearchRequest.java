package io.etrace.api.model.bo.yellowpage;

import lombok.Data;

@Data
public class SearchRequest {

    private boolean searchList;

    private boolean searchRecord;

    private boolean searchKeyWord;

    private String keyword;

    private int pageNum = 1;
    private int pageSize = 10;
}
