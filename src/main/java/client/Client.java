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
                primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(writeMsg.getLoc().getHost(), writeMsg.getLoc().getPort(), writeMsg.getLoc().getRmiKey());
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
        masterStub = (MasterServerClientInterface) RmiRunner.lookupStub(master.getHost(), master.getPort(), master.getRmiKey());
    }

    public FileContent read(String fileName) throws IOException, NotBoundException {
        ReplicaLoc[] replicaLocs = masterStub.read(fileName);
        ReplicaServerClientInterface primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(replicaLocs[0].getHost(), replicaLocs[0].getPort(), replicaLocs[0].getRmiKey());
        return primaryReplicaStub.read(fileName);
    }

    public Transaction createTransaction(){
        return new Transaction();
    }

}
