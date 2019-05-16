import master.MasterServerClientInterface;
import replica.ReplicaServer;
import rmi.RmiRunner;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ReplicaServerMain {

    public static void main(String[] args) {
        if (args.length > 6) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments expected maximum number %d but found %d", 6, args.length));

        }
        int rmiPort = Integer.parseInt(args[0]);
        String rmiKey = args[1];
        String serverHost = args[2];
        int serverPort = Integer.parseInt(args[3]);
        String serverRmiKey = args[4];
        String localAddress = "localhost";
        if (args.length == 6) {
            localAddress = args[5];
        }
        MasterServerClientInterface masterServerStub;
        try {
            masterServerStub = (MasterServerClientInterface) RmiRunner.lookupStub(serverHost, serverPort, serverRmiKey);
        } catch (RemoteException | NotBoundException e) {
            System.out.println(String.format("Cannot find master server stub on host: %s port: %d RMI key: %s",
                                                serverHost, serverPort, serverRmiKey));
            e.printStackTrace();
            return;
        }
        ReplicaServer replicaServer = new ReplicaServer(rmiKey, masterServerStub);
        RmiRunner.createRegistry(localAddress, rmiPort);
        try {
            RmiRunner.publishStub(replicaServer, rmiKey, rmiPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
