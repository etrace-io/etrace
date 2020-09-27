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

package io.etrace.agent.message.manager;

import io.etrace.common.message.trace.TraceContext;

public class DefaultTraceContext implements TraceContext {
    private DefaultMessageManager.Context ctx;

    public DefaultTraceContext(DefaultMessageManager.Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public DefaultMessageManager.Context getCtx() {
        return ctx;
    }
}
