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

package io.etrace.agent.io;

import io.etrace.agent.network.AbstractSocketClient;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SocketClientFactory {

    private static List<AbstractSocketClient> clients = new CopyOnWriteArrayList<>();

    public static Client getClient() {
        Client client = new Client();
        clients.add(client);
        return client;
    }

    public static Client getClient(int senderTimeout) {
        Client client = new Client(senderTimeout);
        clients.add(client);
        return client;
    }

    public static void shutdown() {
        try {
            clients.forEach(client -> {
                try {
                    client.shutdown();
                } finally {
                    //ignore
                }
            });
        } finally {
            //ignore
        }
    }

}
