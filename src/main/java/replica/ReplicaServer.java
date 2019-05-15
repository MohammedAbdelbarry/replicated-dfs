package replica;

import master.FileContent;
import master.MasterServerClientInterface;
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
    private MasterServerClientInterface masterServerStub;
    private Map<String, Long> lockingTransaction;
    private Map<Long, String> transactionFile;
    private Lock lockingTransactionLock;
    private String replicaKey;

    public ReplicaServer(String replicaKey, final MasterServerClientInterface masterServerStub) {
        this.masterServerStub = masterServerStub;
        lockingTransaction = new HashMap<>();
        transactionFile = new ConcurrentHashMap<>();
        lockingTransactionLock = new ReentrantLock();
        this.replicaKey = replicaKey;
        new File(replicaKey).mkdir();
    }

    public WriteMsg write(long txnID, long msgSeqNum, FileContent data) throws RemoteException, IOException {
       String fileName = replicaKey + File.separator + data.getFileName();
        System.out.println(String.format("Write(%d, %s)", txnID, fileName));
        transactionFile.putIfAbsent(txnID, fileName);
        lockingTransactionLock.lock();
        System.out.println(String.format("Lock(%d, %s)", txnID, "lockingTransactionLock"));
        while (lockingTransaction.containsKey(fileName) && lockingTransaction.get(fileName) != txnID) {
            lockingTransactionLock.unlock();
            try {
                Thread.sleep(SLEEP_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lockingTransactionLock.lock();
        }
        lockingTransaction.put(fileName, txnID);
        lockingTransactionLock.unlock();
        System.out.println(String.format("Unlock(%d, %s)", txnID, "lockingTransactionLock"));

        FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(fileName);

        fileHandler.getLock().writeLock().lock();
        System.out.println(String.format("Write-Lock(%d, %s)", txnID, fileName));
        fileHandler.write(data.getData());
        fileHandler.getLock().writeLock().unlock();
        System.out.println(String.format("Write-Unlock(%d, %s)", txnID, fileName));
        return null;
    }

    public FileContent read(String fileName) throws FileNotFoundException, IOException, RemoteException {
        fileName = replicaKey + File.separator + fileName;
        System.out.println(String.format("Read(%s)", fileName));
        FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(fileName);
        fileHandler.getLock().readLock().lock();
        System.out.println(String.format("Read-Lock(%s)", fileName));
        FileContent content = new FileContent(fileName, fileHandler.read());
        fileHandler.getLock().readLock().unlock();
        System.out.println(String.format("Read-Unlock(%s)", fileName));
        return content;
    }

    public boolean update(FileContent content) throws RemoteException {
        File file = new File(replicaKey + File.separator + content.getFileName());
        try {
            Files.write(file.toPath(), content.getData().getBytes(), StandardOpenOption.CREATE);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean commit(long txnID, long numOfMsgs) throws MessageNotFoundException, RemoteException {
        String filePath = transactionFile.get(txnID);
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        ReadWriteLock lock = null;
        try {
            FileHandler fileHandler = FileHandlerPool.getInstance().getHandler(filePath);
            lock = fileHandler.getLock();
            lock.writeLock().lock();
            fileHandler.flush();
            for (ReplicaLoc replica : masterServerStub.getReplicas(fileName)) {
                try {
                    ReplicaServerClientInterface replicaServer = (ReplicaServerClientInterface) RmiRunner.lookupStub(replica.getHost(),
                            replica.getPort(), replica.getRmiKey());
                    String data = new String(Files.readAllBytes(new File(filePath).toPath()));
                    replicaServer.update(new FileContent(fileName, data));
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }

            }
            cleanUp(filePath, txnID);
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
        File file = new File(replicaKey + File.separator + filename);
        return file.exists();
    }

    private void cleanUp(String fileName, long txnID) {
        transactionFile.remove(txnID);
        lockingTransactionLock.lock();
        lockingTransaction.remove(fileName);
        lockingTransactionLock.unlock();
    }
}
