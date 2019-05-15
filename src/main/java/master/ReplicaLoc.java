package master;

public class ReplicaLoc {
    private String host;
    private int port;
    private String rmiKey;

    public ReplicaLoc(final String host, final int port, final String rmiKey){
        this.host = host;
        this.rmiKey = rmiKey;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getRmiKey() {
        return rmiKey;
    }

    @Override
    public String toString() {
        return host + ":" + port + ":" + rmiKey;
    }

    @Override
    public boolean equals(Object o) {
        ReplicaLoc replicaLoc = (ReplicaLoc) o;
        return  this.host.equals(replicaLoc.getHost()) &&
                this.port == replicaLoc.getPort() &&
                this.rmiKey.equals(replicaLoc.getRmiKey());
    }
}
