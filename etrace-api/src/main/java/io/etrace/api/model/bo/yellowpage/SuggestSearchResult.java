package io.etrace.api.model.bo.yellowpage;

import lombok.Data;

@Data
public class SuggestSearchResult {
    private SearchType type;
    private Long id;
    private String name;
    private String icon;
    private String description;
}
