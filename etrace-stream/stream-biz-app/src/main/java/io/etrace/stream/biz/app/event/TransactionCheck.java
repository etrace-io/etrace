package io.etrace.stream.biz.app.event;

public class TransactionCheck {
    private static TransactionCheck instance = new TransactionCheck();
    private final TypeNameCheck typeNameCheck = new TypeNameCheck();

    private TransactionCheck() {

    }

    public static TransactionCheck getInstance() {
        return instance;
    }

    public boolean isTooMany(String appId, String type, String name) {
        return typeNameCheck.isTooMany(appId, type, name);
    }
}

