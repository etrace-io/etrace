package io.etrace.collector.controller;

import io.etrace.collector.cluster.discovery.ServiceInstance;
import io.etrace.collector.sharding.impl.FrontShardIngImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.etrace.collector.controller.ThroughputController.PATH;

@RestController
@RequestMapping(PATH)
public class ThroughputController {
    public static final String PATH = "throughput";

    @Autowired
    private FrontShardIngImpl balanceThroughputService;

    @GetMapping
    public ThroughputData getThroughtput() {
        ThroughputData throughputData = new ThroughputData();
        throughputData.setThroughput(balanceThroughputService.getThroughputSnapshot());
        return throughputData;
    }

    @GetMapping("/all")
    public Object getThroughputDataMap() {
        Map<ServiceInstance, Long> throughput = balanceThroughputService.getClusterThroughput();
        if (null == throughput) {
            return "no data!";
        }
        return throughput.entrySet().stream().sorted((Map.Entry.<ServiceInstance, Long>comparingByValue().reversed()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static class ThroughputData implements Comparable<ThroughputData> {
        long throughput;

        public long getThroughput() {
            return throughput;
        }

        public void setThroughput(long throughput) {
            this.throughput = throughput;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ThroughputData that = (ThroughputData)o;

            if (throughput != that.throughput) { return false; }

            return true;
        }

        @Override
        public int hashCode() {
            return (int)(throughput ^ (throughput >>> 32));
        }

        @Override
        public int compareTo(ThroughputData o) {
            return Long.compare(this.throughput, o.throughput);
        }
    }

}
