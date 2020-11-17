package io.etrace.api.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DBGroup {
    private String database;
    private String name;
}
