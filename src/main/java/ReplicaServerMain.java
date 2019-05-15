import master.MasterServer;
import replica.ReplicaServer;
import rmi.RmiRunner;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ReplicaServerMain {

    public static void main(String[] args) {
        if (args.length != 5) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments expected %d but found %d", 5, args.length));
        }
        int rmiPort = Integer.parseInt(args[0]);
        String rmiKey = args[1];
        String serverHost = args[2];
        int serverPort = Integer.parseInt(args[3]);
        String serverRmiKey = args[4];
        MasterServer masterServerStub;
        try {
            masterServerStub = (MasterServer) RmiRunner.lookupStub(serverHost, serverPort, serverRmiKey);
        } catch (RemoteException | NotBoundException e) {
            System.out.println(String.format("Cannot find master server stub on host: %s port: %d RMI key: %s",
                                                serverHost, serverPort, serverRmiKey));
            e.printStackTrace();
            return;
        }
        ReplicaServer replicaServer = new ReplicaServer(masterServerStub);
        RmiRunner.createRegistry(rmiPort);
        try {
            RmiRunner.publishStub(replicaServer, rmiKey, rmiPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
