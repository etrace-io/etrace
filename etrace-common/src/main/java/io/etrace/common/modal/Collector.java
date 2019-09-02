package io.etrace.common.modal;

public class Collector {
    private String ip;
    private int port;

    public Collector() {}

    public Collector(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Collector)) {
            return false;
        }
        Collector collector = (Collector)object;
        if (ip != null && collector.ip != null && ip.equals(collector.ip)
            && port == collector.port) {
            return true;
        }
        return false;
    }
}
