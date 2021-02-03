package io.etrace.consumer.storage.hbase.impl;

import io.etrace.consumer.storage.hbase.IHBaseStorageService;
import io.etrace.consumer.util.RowKeyUtil;
import org.apache.hadoop.hbase.client.Durability;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service

//@ConditionalOnMissingBean(type= "IHBaseStorageService")
//@ConditionalOnMissingBean(value=IHBaseStorageService.class, search= SearchStrategy.CURRENT)
//@ConditionalOnMissingClass("IHBaseStorageService")
public class DefaultHBaseStorageService implements IHBaseStorageService {

    @Override
    public Put createPut(byte[] row, long ts) {
        Put put = new Put(row, ts);
        put.setDurability(Durability.SKIP_WAL);
        return put;
    }

    @Override
    public Put buildHbasePut(long timestamp, String requestId, short shard, byte[] columnFamily, byte[] qualifier,
                             byte[] qualifierValue) {
        Put put = createPut(RowKeyUtil.build(shard, requestId), timestamp);
        put.addColumn(columnFamily, qualifier, qualifierValue);
        return put;
    }

}
