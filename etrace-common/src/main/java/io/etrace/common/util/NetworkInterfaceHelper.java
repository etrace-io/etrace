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

package io.etrace.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum NetworkInterfaceHelper {
    INSTANCE;
    private static String hostName = null;
    InetAddress local;

    NetworkInterfaceHelper() {
        load();
    }

    public String getLocalHostAddress() {
        return local.getHostAddress();
    }

    protected boolean getOSMatches(String osNamePrefix) {
        String os = System.getProperty("os.name");
        return os != null && os.startsWith(osNamePrefix);
    }

    public synchronized String getLocalHostName() {
        if (hostName == null) {
            try {
                boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
                if (linux) {
                    Process process = null;
                    try {

                        process = Runtime.getRuntime().exec("hostname");
                        InputStream inputStream = process.getInputStream();
                        StringBuilder stringBuilder = new StringBuilder();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        String line = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        if (stringBuilder.length() > 0) {
                            hostName = stringBuilder.toString();
                            return hostName;
                        }
                    } catch (IOException e) {
                        System.err.println(e);
                    } finally {
                        if (process != null) {
                            process.destroy();
                        }
                    }
                }
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostName = local.getHostName();
            }
        }
        return hostName;
    }

    private String getProperty(String name) {
        String value = System.getProperty(name);

        if (value == null) {
            value = System.getenv(name);
        }

        return value;
    }

    private void load() {
        String ip = getProperty("host.ip");

        if (ip != null) {
            try {
                local = InetAddress.getByName(ip);
                return;
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        try {
            List<NetworkInterface> nis = Collections.list(NetworkInterface.getNetworkInterfaces());
            List<InetAddress> addresses = new ArrayList<>();
            InetAddress local = null;

            try {
                for (NetworkInterface ni : nis) {
                    if (ni.isUp()) {
                        addresses.addAll(Collections.list(ni.getInetAddresses()));
                    }
                }
                local = findValidateIp(addresses);
            } catch (Exception e) {
                // ignore
            }
            this.local = local;
        } catch (SocketException ignore) {
        }
    }

    public InetAddress findValidateIp(List<InetAddress> addresses) {
        InetAddress local = null;
        for (InetAddress address : addresses) {
            if (address instanceof Inet4Address) {
                if (address.isLoopbackAddress() || address.isSiteLocalAddress()) {
                    if (local == null) {
                        local = address;
                    } else if (address.isSiteLocalAddress() && !address.isLoopbackAddress()) {
                        local = address;
                    } else if (local.isSiteLocalAddress() && address.isSiteLocalAddress()) {
                        if (local.getHostName().equals(local.getHostAddress())
                            && !address.getHostName().equals(address.getHostAddress())) {
                            local = address;
                        }
                    }
                } else {
                    if (local == null) {
                        local = address;
                    }
                }
            }
        }
        return local;
    }

}
