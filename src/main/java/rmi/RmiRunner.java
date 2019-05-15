package rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RmiRunner {
    public RmiRunner() {

    }

    public void createRegistry(final int registryPort) throws RemoteException {
        LocateRegistry.createRegistry(registryPort);
    }

    public Remote publishStub(final Remote remoteInterface, final String rmiKey,
                                final int registryPort) throws RemoteException {
        Remote stub = UnicastRemoteObject.exportObject(remoteInterface, registryPort);
        LocateRegistry.getRegistry(registryPort).rebind(rmiKey, stub);
        return stub;
    }

    public void unpublishStub(final Remote remoteInterface) throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(remoteInterface, true);
    }

    public Remote lookupStub(final String host, final int port, final String rmiKey) throws RemoteException, NotBoundException {
        return LocateRegistry.getRegistry(host, port).lookup(rmiKey);
    }
}
