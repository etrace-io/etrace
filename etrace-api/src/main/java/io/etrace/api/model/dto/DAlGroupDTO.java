package io.etrace.api.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DAlGroupDTO {

    private String groupName;

    private List<DBGroup> dbGroups;

    private List<DalAppDTO> apps;
}
