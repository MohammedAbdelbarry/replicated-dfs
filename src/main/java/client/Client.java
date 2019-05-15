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

        private WriteMsg writeMsg;
        private ReplicaServerClientInterface primaryReplicaStub;
        private int seqNo;

        public Transaction(String fileName) throws IOException, NotBoundException {
            writeMsg = null;
            primaryReplicaStub = null;
            init(fileName);
        }

        public void init(String fileName) throws IOException, NotBoundException {
            writeMsg = master.write(new FileContent(fileName, null));
            primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(writeMsg.getLoc().getHost(), writeMsg.getLoc().getPort(), writeMsg.getLoc().getRmiKey());
        }

        public void addWrite(FileContent file) throws IOException {
            System.out.println(String.format("AddWrite(%s)", file));
            System.out.println(String.format("primaryReplicaStub=%s", primaryReplicaStub));
            System.out.println(String.format("writeMsg=%s", writeMsg));
            primaryReplicaStub.write(writeMsg.getTransactionId(), seqNo++, file);
        }

        public boolean commit() throws MessageNotFoundException, RemoteException {
            return primaryReplicaStub.commit(writeMsg.getTransactionId(), seqNo);
        }
    }
    
    private MasterServerClientInterface master;


    public Client(MasterServerClientInterface master) throws RemoteException, NotBoundException {
        this.master = master;
    }

    public FileContent read(String fileName) throws IOException, NotBoundException {
        ReplicaLoc[] replicaLocs = master.read(fileName);
        ReplicaServerClientInterface primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(replicaLocs[0].getHost(), replicaLocs[0].getPort(), replicaLocs[0].getRmiKey());
        return primaryReplicaStub.read(fileName);
    }

    public Transaction createTransaction(String fileName) throws IOException, NotBoundException {
        return new Transaction(fileName);
    }


    public static void main(String[] args) throws IOException, NotBoundException, MessageNotFoundException, IllegalArgumentException {

        if (args.length != 3) {
            throw new IllegalArgumentException(String.format("Invalid number of arguments expected %d but found %d", 3, args.length));
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String rmiKey = args[2];

        String fileName = "file.txt";

        MasterServerClientInterface masterStub = (MasterServerClientInterface) RmiRunner.lookupStub(host,
                port, rmiKey);

        Client client = new Client(masterStub);

        try {
            client.read(fileName);
        } catch (FileNotFoundException e) {
            System.out.println("File doesn't exist as expected");
        }

        Transaction t = client.createTransaction(fileName);
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
