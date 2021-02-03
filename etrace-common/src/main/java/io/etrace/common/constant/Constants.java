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

package io.etrace.common.constant;

public interface Constants {
    String ROOT_RPC_ID = "1";
    String AGENT_EVENT_TYPE_TRACE = "Trace";
    String AGENT_EVENT_TYPE_METRIC = "Metric";

    String UNKNOWN_APP_ID = "unknown";

    String UNSET = "unset";
    String SUCCESS = "0";
    String FAILURE = "failure";
    String URL = "URL";
    String SQL = "SQL";
    String SQL_DATABASE = "SQL.database";
    String HEART_BEAT = "Heartbeat";
    String RPC_ID = "rpcid";

    String TYPE_ETRACE_LINK = "ETraceLink";
    String NAME_REMOTE_CALL = "RemoteCall";
    String NAME_ASYNC_CALL = "AsyncCall";
    String NAME_TRUNCATE = "Truncate";
    String NAME_BAD_TRANSACTION = "BadTransaction";

    String SUCCESS_STR = "success";
    String UNKNOWN = "unknown";

    String HEARTBEAT_TYPE_AGENT = "agent-stat";

}
