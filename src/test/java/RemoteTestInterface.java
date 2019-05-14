import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteTestInterface extends Remote {
    int read() throws RemoteException;
}
