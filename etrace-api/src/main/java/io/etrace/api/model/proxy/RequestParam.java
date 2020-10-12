package io.etrace.api.model.proxy;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@Deprecated
public class RequestParam {
    private ReqInfo reqInfo;
    private String method;
    private String reqBody;
    private Map<String, String> param;
}
