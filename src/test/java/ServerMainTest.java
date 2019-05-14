import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMainTest {
    private static final String rmiServerAddress = "127.0.0.1";

    public static void main(String[] args) {
        try {
            RmiRunner serverRmiRunner = new RmiRunner(rmiServerAddress);
            serverRmiRunner.createRegistry();

            /* Server */
            RemoteTestInterfaceImpl impl = new RemoteTestInterfaceImpl();
            RemoteTestInterface stubUnused = (RemoteTestInterface) serverRmiRunner.publishStub(impl, "test");
            System.out.println("Published interface \"test\"");

            /* Clients */
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 5; i++) {
                RmiRunner clientRmiRunner = new RmiRunner(rmiServerAddress);
                executorService.submit(() -> {
                    RemoteTestInterface stub = null;
                    try {
                        stub = (RemoteTestInterface) clientRmiRunner.lookupStub("test");
                        System.out.println(stub.read());
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                });
            }

            /* Server */
            executorService.shutdown();
            executorService.awaitTermination(3, TimeUnit.SECONDS);
            serverRmiRunner.unpublishStub(impl);
        } catch (RemoteException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
