package io.etrace.api.controller.legacy;

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

import static io.etrace.api.config.SwaggerConfig.PROXY_REQUEST_TAG;

@RestController
@RequestMapping(value = "/proxy")
@Api(value = "proxy", description = "代理api", tags = {PROXY_REQUEST_TAG})
public class ProxyController {
    protected static Logger LOGGER = LoggerFactory.getLogger(ProxyController.class);
    @Autowired
    private ProxyService proxyService;

    @ResponseBody
    public ResponseEntity<Object> proxy(@RequestBody String body, HttpMethod method, HttpServletRequest request,
                                        HttpServletResponse response) throws InterruptedException {

        ProxyResponse result = proxyService.getResult00(request, method, body, "/proxy");
        return ResponseEntity.status(result.responseStatus).body(result.toString());
    }
}
