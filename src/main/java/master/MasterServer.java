package master;

import replica.ReplicaServerClientInterface;
import rmi.RmiRunner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class MasterServer implements MasterServerClientInterface {
    private ArrayList<ReplicaLoc> replicaServers;
    private HashMap<String, ReplicaLoc> primaryReplicas;
    private HashMap<String, ArrayList<ReplicaLoc>> fileToReplicas;
    private Lock replicaLock;
    private AtomicLong lastTransaction;
    private int replicationFactor;
    private Timer heartbeatTimer;

    public MasterServer(final String replicasFilePath, final int replicationFactor) {
        fileToReplicas = new HashMap<>();
        primaryReplicas = new HashMap<>();
        replicaServers = new ArrayList<>();
        replicaLock = new ReentrantLock();
        heartbeatTimer = new Timer(true);
        this.replicationFactor = replicationFactor;
        this.lastTransaction = new AtomicLong(-1);

        try {
            BufferedReader bufferReader = new BufferedReader(new FileReader(replicasFilePath));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                String replicaHost = tokens[0];
                int replicaPort = Integer.parseInt(tokens[1]);
                String replicaRmiKey = tokens[2];
                replicaServers.add(new ReplicaLoc(replicaHost, replicaPort, replicaRmiKey));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //TODO: Uncomment this
//        this.trackHeartbeats();
    }

    public ReplicaLoc[] read(final String fileName) throws FileNotFoundException,
            IOException, RemoteException, NotBoundException {
        System.out.println(String.format("Read(%s)", fileName));
        replicaLock.lock();
        System.out.println(String.format("Lock(%s)", "replicaLock"));
        if (!primaryReplicas.containsKey(fileName)) {
            System.out.println(String.format("No-Primary-Replica(%s)", fileName));
            replicaLock.unlock();
            throw new FileNotFoundException();
        }

        ReplicaLoc primaryReplica = primaryReplicas.get(fileName);

        // call primary replica to check if file exists
        ReplicaServerClientInterface primaryReplicaStub = (ReplicaServerClientInterface) RmiRunner.lookupStub(primaryReplica.getHost(),
                                                                primaryReplica.getPort(), primaryReplica.getRmiKey());

        if (!primaryReplicaStub.fileExists(fileName)) {
            System.out.println(String.format("No-File(%s)", fileName));
            replicaLock.unlock();
            throw new FileNotFoundException();
        }

        ArrayList<ReplicaLoc> replicas = new ArrayList<>(fileToReplicas.get(fileName));
        replicaLock.unlock();
        System.out.println(String.format("Unlock(%s)", "replicaLock"));
        replicas.add(primaryReplica);
        return replicas.toArray(new ReplicaLoc[replicas.size()]);
    }

    public WriteMsg write(FileContent file) throws RemoteException, IOException {
        System.out.println(String.format("Write(%s)", file.getFileName()));
        Date date = new Date();
        long timestamp = date.getTime();

        replicaLock.lock();
        System.out.println(String.format("Lock(%s)", file.getFileName()));
        if (!primaryReplicas.containsKey(file.getFileName())) {
            System.out.println("Trying to sample random numbers");
            int[] rand = new Random().ints(0,replicaServers.size())
                    .distinct().limit(Math.min(replicaServers.size(), replicationFactor)).toArray();
            System.out.println(String.format("Random(replicationFactor=%d)=%s", 3, Arrays.toString(rand)));
            primaryReplicas.put(file.getFileName(), replicaServers.get(rand[0]));

            ArrayList<ReplicaLoc> replicas = new ArrayList<>();
            for (int i = 1; i < rand.length; i++) {
                replicas.add(replicaServers.get(rand[i]));
            }
            fileToReplicas.put(file.getFileName(), replicas);
        }
        ReplicaLoc primaryReplica = primaryReplicas.get(file.getFileName());
        replicaLock.unlock();
        System.out.println(String.format("Unlock(%s)", file.getFileName()));

        return new WriteMsg(lastTransaction.incrementAndGet(), timestamp, primaryReplica);
    }

    public Collection<ReplicaLoc> getReplicas(String fileName) {
        return fileToReplicas.get(fileName);
    }

    public void trackHeartbeats() {
        TimerTask heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                replicaLock.lock();
                replicaServers.forEach(replicaServer -> {
                    RmiRunner replicaRmiRunner = new RmiRunner();
                    try {
                        ReplicaServerClientInterface stub = (ReplicaServerClientInterface) replicaRmiRunner.lookupStub(
                                replicaServer.getHost(), replicaServer.getPort(), replicaServer.getRmiKey());
                        stub.isAlive();
                    } catch (RemoteException | NotBoundException e) {
                        System.err.println("Replica server: " + replicaServer + " went down!");
                        /* Assign the files belonging primarily to this primary replica to another replica
                           making it the new primary replica */
                        List<String> filesInFailingReplica = primaryReplicas.keySet().stream().filter(
                                file -> primaryReplicas.get(file).equals(replicaServer)).collect(Collectors.toList());
                        filesInFailingReplica.forEach(file -> primaryReplicas.put(
                                file, replicaServers.get(new Random().nextInt(replicaServers.size()))));
                        filesInFailingReplica.forEach(file -> {
                            fileToReplicas.get(file).remove(primaryReplicas.get(file));
                            fileToReplicas.get(file).add(replicaServer);
                        });
                    }
                });
                replicaLock.unlock();
            }
        };
        heartbeatTimer.scheduleAtFixedRate(heartbeatTask, 0, 1000);
    }
}
