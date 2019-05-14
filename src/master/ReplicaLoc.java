package master;

import java.net.InetAddress;

public class ReplicaLoc {
    private String ip;
    private int port;

    public ReplicaLoc(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }
}
