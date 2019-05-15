package master;

public class ReplicaLoc {
    private String ip;
    private String rmiKey;
    private int port;

    public ReplicaLoc(final String ip, final String rmiKey, final int port){
        this.ip = ip;
        this.rmiKey = rmiKey;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public String getRmiKey() {
        return rmiKey;
    }

    public int getPort() {
        return port;
    }
}
