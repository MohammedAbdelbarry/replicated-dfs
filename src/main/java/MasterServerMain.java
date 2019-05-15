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
        MasterServer masterServer = new MasterServer(replicasFilePath, replicationFactor);
        RmiRunner rmiRunner = new RmiRunner();
        try {
            rmiRunner.createRegistry(rmiPort);
            rmiRunner.publishStub(masterServer, rmiKey, rmiPort);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
