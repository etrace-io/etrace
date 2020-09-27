/*
 * Copyright 2020 etrace.io
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

package io.etrace.common.message.trace;

import io.etrace.common.message.agentconfig.ConfigManger;

/**
 * This interface is internal usage, application developer should never call this method.
 */
public interface TraceManager {

    void addNonTransaction(Message message);

    void startTransaction(Transaction transaction);

    void endTransaction(Transaction transaction);

    void setup();

    void setup(String requestId, String rpcId);

    void importContext(TraceContext ctx);

    TraceContext exportContext();

    String getClientAppId();

    boolean hasContext();

    void removeContext();

    String nextRemoteRpcId();

    String nextLocalRpcId();

    String getCurrentRequestId();

    String getRpcId();

    @Deprecated
    String getCurrentRpcIdAndCurrentCall();

    boolean isImportContext();

    /**
     * Destroy current thread local data
     */
    void reset();

    ConfigManger getConfigManager();

    void shutdown();

    boolean hasTransaction();

}
