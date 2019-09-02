package io.etrace.agent.message.io;

import io.etrace.agent.message.network.AbstractSocketClient;

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
