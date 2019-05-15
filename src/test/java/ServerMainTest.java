import rmi.RmiRunner;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerMainTest {
    private static final String RMI_SERVER_ADDR = "127.0.0.1";
    private static final int RMI_SERVER_PORT = 1099;

    public static void main(String[] args) {
        try {
            RmiRunner serverRmiRunner = new RmiRunner(RMI_SERVER_ADDR);
            serverRmiRunner.createRegistry(RMI_SERVER_PORT);

            /* Server */
            RemoteTestInterfaceImpl impl = new RemoteTestInterfaceImpl();
            RemoteTestInterface stubUnused = (RemoteTestInterface) serverRmiRunner.publishStub(impl, "test", RMI_SERVER_PORT);
            System.out.println("Published interface \"test\"");

            /* Clients */
            ExecutorService executorService = Executors.newFixedThreadPool(5);
            for (int i = 0; i < 5; i++) {
                RmiRunner clientRmiRunner = new RmiRunner(RMI_SERVER_ADDR);
                executorService.submit(() -> {
                    RemoteTestInterface stub = null;
                    try {
                        stub = (RemoteTestInterface) clientRmiRunner.lookupStub("localhost", RMI_SERVER_PORT, "test");
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
