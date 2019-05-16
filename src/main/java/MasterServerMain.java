import master.MasterConfiguration;
import master.MasterServer;
import rmi.RmiRunner;

import java.rmi.RemoteException;

public class MasterServerMain {

    public static void main(String[] args) {
        String configurationFilePath = "masterServerConfig.properties";
        MasterConfiguration masterConfiguration = new MasterConfiguration(configurationFilePath);
        int rmiPort = masterConfiguration.getRmiPort();
        String rmiKey = masterConfiguration.getRmiKey();
        int replicationFactor = masterConfiguration.getReplicationFactor();
        String replicasFilePath = masterConfiguration.getReplicasFilePath();
        String localAddress = masterConfiguration.getLocalAddress();
        MasterServer masterServer = new MasterServer(replicasFilePath, replicationFactor);
        RmiRunner.createRegistry(localAddress, rmiPort);
        try {
            RmiRunner.publishStub(masterServer, rmiKey, rmiPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
