package io.etrace.collector;

import org.apache.curator.test.TestingServer;

public class BaseTest {

    public void startZookeeperServer() {
        try {
            new TestingServer(2181);
            System.out.println("zookeeper服务启动正常!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
