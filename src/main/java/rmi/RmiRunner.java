package rmi;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RmiRunner {
    private static final int REGISTRY_PORT = 1099;

    public RmiRunner(String rmiServerAddress) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", rmiServerAddress);
    }

    public void createRegistry() throws RemoteException {
        LocateRegistry.createRegistry(REGISTRY_PORT);
    }

    public Remote publishStub(final Remote remoteInterface, final String rmiKey) throws RemoteException {
        Remote stub = UnicastRemoteObject.exportObject(remoteInterface, 0);
        LocateRegistry.getRegistry().rebind(rmiKey, stub);
        return stub;
    }

    public void unpublishStub(final Remote remoteInterface) throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(remoteInterface, true);
    }

    public Remote lookupStub(final String rmiKey) throws RemoteException, NotBoundException {
        return LocateRegistry.getRegistry(null).lookup(rmiKey);
    }
}
