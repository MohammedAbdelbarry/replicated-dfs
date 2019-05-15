package replica;

import master.FileContent;
import master.MessageNotFoundException;
import master.WriteMsg;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ReplicaServerClientInterface extends Remote {
	/**
	 * 
	 * @param txnID
	 *            : the ID of the transaction to which this message relates
	 * @param msgSeqNum
	 *            : the message sequence number. Each transaction starts with
	 *            message sequence number 1.
	 * @param data
	 *            : data to write in the file
	 * @return message with required info
	 * @throws IOException
	 * @throws RemoteException
	 */
    WriteMsg write(long txnID, long msgSeqNum, FileContent data)
			throws RemoteException, IOException;
	
	FileContent read(String fileName) throws FileNotFoundException,
	IOException, RemoteException;

    boolean update(FileContent content) throws RemoteException;

    /**
     *
     * @param txnID
     *            : the ID of the transaction to which this message relates
     * @param numOfMsgs
     *            : Number of messages sent to the server
     * @return true for acknowledgment
     * @throws MessageNotFoundException
     * @throws RemoteException
     */
    boolean commit(long txnID, long numOfMsgs)
        throws MessageNotFoundException, RemoteException;
	
	/**
	 * * @param txnID: the ID of the transaction to which this message relates
	 * 
	 * @return true for acknowledgment
	 * @throws RemoteException
	 */
    boolean abort(long txnID) throws RemoteException;
}
