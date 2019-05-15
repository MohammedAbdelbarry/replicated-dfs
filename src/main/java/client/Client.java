package client;

import master.FileContent;
import master.MasterServerClientInterface;
import master.MessageNotFoundException;
import master.ReplicaLoc;
import master.WriteMsg;
import replica.ReplicaServerClientInterface;
import rmi.RmiRunner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Client {

    public class Transaction {

        private MasterServerClientInterface master;

        private WriteMsg writeMsg;
        private ReplicaServerClientInterface primaryReplicaStub;
        private int seqNo;

        public Transaction() {
            writeMsg = null;
            primaryReplicaStub = null;
        }

        public void addWrite(FileContent file) throws IOException, NotBoundException {
            if (primaryReplicaStub != null && writeMsg == null) {
                WriteMsg writeMsg = master.write(file);
                primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(writeMsg.getLoc().getHost(), writeMsg.getLoc().getPort(), writeMsg.getLoc().getRmiKey());
                primaryReplicaStub.write(writeMsg.getTransactionId(), seqNo++, file);
            } else {
                primaryReplicaStub.write(writeMsg.getTransactionId(), seqNo++, file);
            }
        }

        public boolean commit() throws MessageNotFoundException, RemoteException {
            if (primaryReplicaStub != null && writeMsg != null) {
                return primaryReplicaStub.commit(writeMsg.getTransactionId(), seqNo);
            } else {
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


    public static void main(String[] args) throws IOException, NotBoundException, MessageNotFoundException, IllegalArgumentException {

        if (args.length != 3) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments expected %d but found %d", 3, args.length));
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String rmiKey = args[2];

        String fileName = "file.txt";

        ReplicaLoc replicaLoc = new ReplicaLoc(host, port, rmiKey);
        Client client = new Client(replicaLoc);

        try {
            client.read(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist as expected");
        }

        Transaction t = client.createTransaction();
        t.addWrite(new FileContent(fileName, "datadatadata1"));
        t.addWrite(new FileContent(fileName, "datadatadata2"));

        t.commit();

        try {
            client.read(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist");
        }
    }


}
