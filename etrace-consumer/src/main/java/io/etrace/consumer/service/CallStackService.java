/*
 * Copyright 2019 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.consumer.service;

import io.etrace.common.message.trace.CallStackV1;
import io.etrace.common.message.trace.MessageId;
import io.etrace.consumer.exception.CallStackNotFoundException;
import io.etrace.consumer.model.BlockIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CallStackService {
    private final Logger LOGGER = LoggerFactory.getLogger(CallStackService.class);

    @Autowired
    private HBaseStackDao hBaseStackDao;
    @Autowired
    private HDFSService hdfsService;

    public CallStackV1 queryByMessageId(String messageId) throws Exception {
        MessageId id = MessageId.parse(messageId);
        if (null == id) {
            throw new CallStackNotFoundException("messageId illegal, should be '{requestId}$${rpcId}': " + messageId);
        }

        try {
            BlockIndex blockIndex = hBaseStackDao.findBlockIndex(id);
            if (null == blockIndex) {
                throw new CallStackNotFoundException("HBase没有找到相应的记录,requestId:".concat(messageId));
            }
            return hdfsService.findMessage(blockIndex);
        } catch (Throwable e) {
            LOGGER.error("find block index error:", e);
        }
        return null;
    }

}
