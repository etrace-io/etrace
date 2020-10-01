package io.etrace;

import io.etrace.stream.biz.app.ApplicationCallStackDecode;
import io.etrace.stream.biz.app.CallStackDecode;
import io.etrace.stream.core.model.Event;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OpenSourceApplicationCallStackDecodeTest {

    private CallStackDecode decode = new ApplicationCallStackDecode();

    @Test
    public void parseOne() throws IOException {
        String data = "[\"#v1#t1\",\"one_appId\",\"127.0.0.1\",\"my_hostname\",\"requestId\",\"id\",[\"transaction\","
            + "\"TestTransaction_type\",\"TestTransaction_ame\",\"unset\",0,1583226033322,true,{\"bbbbb\":\"ccccc\"},"
            + "4,null],{\"cluster\":\"cluster_value\",\"instance\":\"instance_value\",\"mesosTaskId\":\"taskId\","
            + "\"eleapposSlaveFqdn\":\"apposFqdn\",\"ezone\":\"ezone_value\",\"idc\":\"idc_value\","
            + "\"eleapposLabel\":\"apposLable\"}]";
        List<Event> events = decode.decode(data.getBytes());
        assertEquals(1, events.size());
    }

}
