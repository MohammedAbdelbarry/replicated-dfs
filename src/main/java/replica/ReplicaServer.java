package replica;

import master.FileContent;
import master.MessageNotFoundException;
import master.WriteMsg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

public class ReplicaServer implements ReplicaServerClientInterface {
    public WriteMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {
        return null;
    }

    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        return null;
    }

    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, RemoteException {
        return false;
    }

    public boolean abort(long txnID) throws RemoteException {
        return false;
    }
}
