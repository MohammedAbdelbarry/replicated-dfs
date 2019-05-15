package replica;

import master.FileContent;
import master.MasterServer;
import master.MessageNotFoundException;
import master.ReplicaLoc;
import master.WriteMsg;
import rmi.RmiRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class ReplicaServer implements ReplicaServerClientInterface {
    private static final int SLEEP_DURATION = 100;
    private MasterServer masterServerStub;
    private Map<String, Long> lockingTransaction;
    private Map<Long, String> transactionFile;
    private Lock lockingTransactionLock;

    public ReplicaServer(final MasterServer masterServerStub) {
        this.masterServerStub = masterServerStub;
        lockingTransaction = new HashMap<>();
        transactionFile = new ConcurrentHashMap<>();
        lockingTransactionLock = new ReentrantLock();
    }

    public WriteMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {
        transactionFile.putIfAbsent(txnID, data.getFileName());
        lockingTransactionLock.lock();
        while (lockingTransaction.containsKey(data.getFileName()) && lockingTransaction.get(data.getFileName()) != txnID) {
            lockingTransactionLock.unlock();
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lockingTransactionLock.lock();
        }
        lockingTransaction.put(data.getFileName(), txnID);
        lockingTransactionLock.unlock();

        FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(data.getFileName());

        fileHandler.getLock().writeLock().lock();
        fileHandler.write(data.getData());
        fileHandler.getLock().writeLock().unlock();
        return null;
    }

    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(fileName);
        fileHandler.getLock().readLock().lock();
        FileContent content = new FileContent(fileName, fileHandler.read());
        fileHandler.getLock().readLock().unlock();
        return content;
    }

    public boolean update(FileContent content) throws RemoteException {
        File file = new File(content.getFileName());
        try {
            Files.write(file.toPath(), content.getData().getBytes(), StandardOpenOption.CREATE);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, RemoteException {
        String fileName = transactionFile.get(txnID);
        ReadWriteLock lock = null;
        try {
            FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(fileName);
            lock = fileHandler.getLock();
            lock.writeLock().lock();
            fileHandler.flush();
            for (ReplicaLoc replica : masterServerStub.getReplicas(fileName)) {
                try {
                    ReplicaServerClientInterface replicaServer = (ReplicaServerClientInterface) RmiRunner.lookupStub(replica.getHost(),
                            replica.getPort(), replica.getRmiKey());
                    String data = new String(Files.readAllBytes(new File(fileName).toPath()));
                    replicaServer.update(new FileContent(fileName, data));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }

            }
            cleanUp(fileName, txnID);
            lock.writeLock().unlock();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (lock != null) {
            lock.writeLock().unlock();
        }

        return false;
    }

    public boolean abort(long txnID) throws RemoteException {
        String fileName = transactionFile.get(txnID);
        cleanUp(fileName, txnID);
        return true;
    }

    public boolean fileExists(final String filename) throws RemoteException {
        File file = new File(filename);
        return file.exists();
    }

    private void cleanUp(String fileName, long txnID) {
        transactionFile.remove(txnID);
        lockingTransactionLock.lock();
        lockingTransaction.remove(fileName);
        lockingTransactionLock.unlock();
    }
}
