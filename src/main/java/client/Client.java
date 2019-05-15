package client;

import master.FileContent;
import master.MasterServerClientInterface;
import master.ReplicaLoc;
import replica.ReplicaServer;
import replica.ReplicaServerClientInterface;
import rmi.RmiRunner;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {


    public class Transaction {

        private int size;


        public void addWrite(FileContent file) {

        }

        public boolean commit(){

        }


    }



    private ReplicaLoc master;
    private MasterServerClientInterface masterStub;

    public Client(ReplicaLoc master) throws RemoteException, NotBoundException {
        this.master = master;
        RmiRunner rmiRunner = new RmiRunner(master.getIp());
        masterStub = (MasterServerClientInterface) rmiRunner.lookupStub(master.getRmiKey());
    }

    public FileContent read(String fileName) throws IOException, NotBoundException {
        ReplicaLoc[] replicaLocs = masterStub.read(fileName);
        RmiRunner rmiRunner = new RmiRunner(replicaLocs[0].getIp());
        ReplicaServerClientInterface primaryReplicaStub = (ReplicaServerClientInterface) rmiRunner.lookupStub(replicaLocs[0].getRmiKey());
        return primaryReplicaStub.read(fileName);
    }


    public






}
