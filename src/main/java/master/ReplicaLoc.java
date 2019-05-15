package master;

import java.net.InetAddress;

public class ReplicaLoc {
    private String ip;
    private String rmiKey;

    public ReplicaLoc(String ip, String rmiKey){
        this.ip = ip;
        this.rmiKey = rmiKey;
    }

    public String getIp() {
        return ip;
    }

    public String getRmiKey() {
        return rmiKey;
    }
}
