package io.etrace.api.controller.openapi;

import io.etrace.api.model.ProxyResponse;
import io.etrace.api.service.ProxyService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.etrace.api.config.SwaggerConfig.OPEN_API_TAG;

@RestController
@RequestMapping(value = "/api")
@Api(value = "/api", tags = OPEN_API_TAG)
public class ApiProxyController {
    protected static Logger LOGGER = LoggerFactory.getLogger(ApiProxyController.class);

    @Autowired
    private ProxyService proxyService;

    @RequestMapping(value = "/v1/proxy/**")
    @ResponseBody
    public ResponseEntity<Object> proxy(@RequestBody String body, HttpMethod method, HttpServletRequest request,
                                        HttpServletResponse response) throws InterruptedException {
        ProxyResponse result = proxyService.getResult00(request, method, body, "/api/v1/proxy");
        return ResponseEntity.status(result.responseStatus).body(result.toString());
    }
}
