import replica.ReplicaServer;
import rmi.RmiRunner;

import java.rmi.RemoteException;

public class ReplicaServerMain {

    public static void main(String[] args) {
        if (args.length > 4) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        int rmiPort = Integer.parseInt(args[0]);
        String rmiKey = args[1];
        String serverHost = args[2];
        int serverPort = Integer.parseInt(args[3]);
        ReplicaServer replicaServer = new ReplicaServer(serverHost, serverPort);
        RmiRunner rmiRunner = new RmiRunner();
        rmiRunner.createRegistry(rmiPort);
        try {
            rmiRunner.publishStub(replicaServer, rmiKey, rmiPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
