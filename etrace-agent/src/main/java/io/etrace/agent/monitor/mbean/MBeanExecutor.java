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

package io.etrace.agent.monitor.mbean;

import io.etrace.agent.config.AgentConfiguration;
import io.etrace.common.util.Pair;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;

public class MBeanExecutor {

    public Map<String, List<Pair<String, String>>> execute() {
        Map<String, List<Pair<String, String>>> m = null;
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<String> objectNames = AgentConfiguration.getBeanObjectNames();
            if (objectNames == null) {
                return null;
            }
            for (String objectName : objectNames) {
                try {
                    List<Pair<String, String>> attrs = getAttrs(mBeanServer, objectName);
                    if (attrs == null || attrs.isEmpty()) {
                        continue;
                    }
                    if (m == null) {
                        m = new LinkedHashMap<>();
                    }
                    m.put(objectName, attrs);
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {
        }
        return m;
    }

    private List<Pair<String, String>> getAttrs(MBeanServer mBeanServer, String objectName)
        throws MalformedObjectNameException, IntrospectionException, InstanceNotFoundException, ReflectionException {
        ObjectName on = new ObjectName(objectName);
        MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(on);
        if (mBeanInfo == null) {
            return null;
        }
        MBeanAttributeInfo[] attributeInfos = mBeanInfo.getAttributes();
        if (attributeInfos == null) {
            return null;
        }
        List<Pair<String, String>> attrs = new ArrayList<>();
        for (MBeanAttributeInfo mat : attributeInfos) {
            try {
                Object attrValue = mBeanServer.getAttribute(on, mat.getName());
                if (attrValue != null) {
                    attrs.add(new Pair<>(mat.getName(), String.valueOf(attrValue)));
                }
            } catch (Exception ignore) {
            }
        }
        return attrs;
    }

    public boolean needMBean() {
        Set<String> objectNames = AgentConfiguration.getBeanObjectNames();
        return objectNames != null && !objectNames.isEmpty();
    }

}
