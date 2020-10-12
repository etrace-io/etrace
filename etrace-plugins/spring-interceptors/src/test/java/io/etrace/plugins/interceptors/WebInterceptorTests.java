package io.etrace.plugins.interceptors;

import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class WebInterceptorTests {

    private MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");

    private MockHttpServletResponse response = new MockHttpServletResponse();

    //    @Autowired
    //    private RequestMappingHandlerAdapter handlerAdapter;
    //
    //    @Autowired
    //    private RequestMappingHandlerMapping handlerMapping;
    //
    //    @Test
    //    public void testInterceptor() throws Exception{
    //
    //
    //        MockHttpServletRequest request = new MockHttpServletRequest();
    //        request.setRequestURI("/test");
    //        request.setMethod("GET");
    //
    //
    //        MockHttpServletResponse response = new MockHttpServletResponse();
    //
    //        HandlerExecutionChain handlerExecutionChain = handlerMapping.getHandler(request);
    //
    //        HandlerInterceptor[] interceptors = handlerExecutionChain.getInterceptors();
    //
    //        for(HandlerInterceptor interceptor : interceptors){
    //            interceptor.preHandle(request, response, handlerExecutionChain.getHandler());
    //        }
    //
    //        ModelAndView mav = handlerAdapter. handle(request, response, handlerExecutionChain.getHandler());
    //
    //        for(HandlerInterceptor interceptor : interceptors){
    //            interceptor.postHandle(request, response, handlerExecutionChain.getHandler(), mav);
    //        }
    //
    //        assertEquals(200, response.getStatus());
    //        //assert the success of your interceptor
    //
    //    }

    //    @Test
    //    public void cacheResourcesConfiguration() throws Exception {
    //        WebContentInterceptor interceptor = new WebContentInterceptor();
    //        interceptor.setCacheSeconds(10);
    //
    //        interceptor.preHandle(request, response, null);
    //
    //        Iterable<String> cacheControlHeaders = response.getHeaders("Cache-Control");
    //        assertThat(cacheControlHeaders).contains("max-age=10");
    //
    //        //HttpMetricInterceptor
    //    }
    //
    @Test
    public void testHttpMetricInterceptor() throws Exception {
        HttpMetricInterceptor interceptor = new HttpMetricInterceptor(false, Tags.empty(), Lists.newArrayList());

        interceptor.preHandle(request, response, null);

        interceptor.afterCompletion(request, response, null, null);

        List<Meter> meters = Metrics.globalRegistry.getMeters();
        assertThat(meters.size()).isEqualTo(1);
        Meter meter = meters.get(0);

        //        Iterable<String> cacheControlHeaders = response.getHeaders("Cache-Control");
        //        assertThat(cacheControlHeaders).contains("max-age=10");

        //HttpMetricInterceptor
    }

}
