package rmi;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class RmiRunner {
    public RmiRunner() {

    }

    public static boolean createRegistry(String host, final int registryPort) {
        System.setProperty("java.rmi.server.hostname", host);
        try {
            LocateRegistry.createRegistry(registryPort);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Remote publishStub(final Remote remoteInterface, final String rmiKey,
                                final int registryPort) throws RemoteException {
        Remote stub = UnicastRemoteObject.exportObject(remoteInterface, registryPort);
        LocateRegistry.getRegistry(registryPort).rebind(rmiKey, stub);
        return stub;
    }

    public static void unpublishStub(final Remote remoteInterface) throws NoSuchObjectException {
        UnicastRemoteObject.unexportObject(remoteInterface, true);
    }

    public static Remote lookupStub(final String host, final int port, final String rmiKey) throws RemoteException, NotBoundException {
        return LocateRegistry.getRegistry(host, port).lookup(rmiKey);
    }
}
