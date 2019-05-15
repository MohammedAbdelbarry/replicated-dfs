package client;

import master.*;
import replica.ReplicaServer;
import replica.ReplicaServerClientInterface;
import rmi.RmiRunner;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public class Transaction {

        private MasterServerClientInterface master;

        private WriteMsg writeMsg;
        private ReplicaServerClientInterface primaryReplicaStub;
        private int seqNo;

        public Transaction(){
            writeMsg = null;
            primaryReplicaStub = null;
        }

        public void addWrite(FileContent file) throws IOException, NotBoundException {
            if(primaryReplicaStub != null && writeMsg == null){
                WriteMsg writeMsg = master.write(file);
                RmiRunner rmiRunner = new RmiRunner(writeMsg.getLoc().getIp());
                primaryReplicaStub = (ReplicaServerClientInterface) rmiRunner.lookupStub(writeMsg.getLoc().getIp());
                primaryReplicaStub.write(writeMsg.getTransactionId(), seqNo++, file);
            }else{
                primaryReplicaStub.write(writeMsg.getTransactionId(), seqNo++, file);
            }
        }

        public boolean commit() throws MessageNotFoundException, RemoteException {
            if(primaryReplicaStub != null && writeMsg != null){
                return primaryReplicaStub.commit(writeMsg.getTransactionId(), seqNo);
            }else{
                return true; //empty transaction
            }
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

    public Transaction createTransaction(){
        return new Transaction();
    }

}
