package master;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface MasterServerClientInterface extends Remote {
	/**
	 * Read file from server
	 * 
	 * @param fileName
	 * @return the addresses of  of its different replicas
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws RemoteException
	 */
	public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
			IOException, RemoteException, NotBoundException;

	/**
	 * Start a new write transaction
	 * 
	 * @param data
	 * @return the requiref info
	 * @throws RemoteException
	 * @throws IOException
	 */
	public WriteMsg write(FileContent data) throws RemoteException, IOException;

	public Collection<ReplicaLoc> getReplicas(String fileName) throws RemoteException;
}
