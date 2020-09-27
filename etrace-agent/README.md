## 已知的待改进点

1. `AbstractSocketClient`移除了closeConnectionWhenTimeout()函数，即未处理TCPSender超时时的场景。待不上：TCP发送超时的处理。

2. 针对Redis的特出操作，需简化（因为外部无埋点注入过的Jedis)或去除。

3. 移除AgentConfiguration中 eleme特有的信息，如: ezone, mesosTask, fqdn等

4. TestHeartBeat单元测试无法通过
    1. 会多一个"Thread-Dump"的Transaction出来
    2. 多跑多个Test Suite, 由于"Trace"已经初始化了。期望的改变Trace的HeartBeat upload interval行为未生效。
    3. **在IntelliJ IDea中应能调整'junit test'配置中的"fork mode"为"class"解决多个Test公用一个JVM的问题**

## 已知的改动点:

1. 移除了`MetricHeader`中的`topic`字段。

2. 移除了"BusinessException"的判断处理逻辑(in MessageProducer.java)

3. 重命名了DefaultMessageManager.getCurrentRpcId为getCurrentRpcIdAndCurrentCall

4. 重命名CallStackProducer为MessageQueue;  ProducerContext => QueueContext

6. 移除了 TRACE.redis的相关操作（开源版本不需要）,不过API仍保留

7. 将之前反直觉的异步线程RPCID生成规则（"^"）调整成正常的生成规则。


# 重要: Callstack/Metric的格式说明：

下午中第一图中第一个表格是Callstack，第二个表格是Metric。
最大的区别是，第一个String为"#v1#t1"，或"#v1#t2"。

"v"后面是版本号的数字，以后可根据版本号扩展。
"t"后面是数据类型的标记（Callstack还是Metric），如此才有可能不基于MessageHeader就能解析数据。

```
+------+------------------------------------------------+----------------+----------------------------------------------------------+
|      | AppId   HostIp   HostName    RequestId   RpcId | Message(Array) | Cluster   EZone   Idc    Mesos   Label   Fqdn   Instance |
|      |                                                |                |                                                          |
|      +----------------------------------------------------------------------------------------------------------------------------+
|#v1#t1| AppId   HostIp   HostName    RequestId   RpcId | Message(Array) | Extra Properties(MapvString, Stringv)                    |
|      |                                                |                |                                                          |
+------+------------------------------------------------+----------------+----------------------------------------------------------+

       +----------------------------------+----------------------------------------------+------------------+
       | Topic |AppId   HostIp   HostName | Cluster   EZone   Idc   Mesos   Label   Fqdn | Metric(Array)    |
       |       |                          |                                              |                  |
       +--------------------------------------------------+------------------------------+------------------+
       |       |                          |               |                                                 |
       | #v1#t2|AppId   HostIp   HostName | Metric(Array) | Extra Properties(Map<String, String>)           |
       |       |                          |               |                                                 |
       +-------+--------------------------+---------------+-------------------------------------------------+

```