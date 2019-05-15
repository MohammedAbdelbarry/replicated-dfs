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
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MasterServer implements MasterServerClientInterface {

    private HashMap<String, ReplicaLoc> primaryReplicas;
    private ArrayList<ReplicaLoc> replicaServers;
    private HashMap<String, ArrayList<ReplicaLoc>> fileToReplicas;
    private long lastTransaction;
    private int replicaFactor;

    public MasterServer(String replicaFilePath, int replicaFactor){
        fileToReplicas = new HashMap<>();
        primaryReplicas = new HashMap<>();
        replicaServers = new ArrayList<>();
        this.replicaFactor = replicaFactor;
        this.lastTransaction = -1;
        try {
            BufferedReader bufferreader = new BufferedReader(new FileReader(replicaFilePath));
            String line;
            while ((line = bufferreader.readLine()) != null) {
                String[] tokens = line.split(" ");
                replicaServers.add(new ReplicaLoc(tokens[0], tokens[1]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
            IOException, RemoteException, NotBoundException {
        if(!primaryReplicas.containsKey(fileName)){
            throw new FileNotFoundException();
        }

        ReplicaLoc primaryReplica = primaryReplicas.get(fileName);

        // call primary replica to check if file exists replicaLoc.getIp();
        RmiRunner rmiRunner = new RmiRunner(primaryReplica.getIp());
        ReplicaServerClientInterface primaryReplicaStub = (ReplicaServerClientInterface) rmiRunner.lookupStub(primaryReplica.getRmiKey());

        //boolean fileExists = primaryReplicaStub.fileExists(fileName);
        boolean fileExists = true;
        if(!fileExists){
            throw new FileNotFoundException();
        }

        ArrayList<ReplicaLoc> replicas = new ArrayList<>();
   ;    replicas.addAll(fileToReplicas.get(fileName));
        replicas.add(primaryReplica);
        return replicas.toArray(new ReplicaLoc[replicas.size()]);
    }

    public WriteMsg write(FileContent file) throws RemoteException, IOException{
        Date date = new Date();
        long timestamp = date.getTime();
        if(!primaryReplicas.containsKey(file.getFileName())){
            int[] rand = new Random().ints(0,replicaServers.size()).distinct().limit(replicaFactor).toArray();
            primaryReplicas.put(file.getFileName(), replicaServers.get(rand[0]));

            ArrayList<ReplicaLoc> replicas = new ArrayList<>();
            for(int i = 1; i < rand.length; i++){
                replicas.add(replicaServers.get(rand[i]));
            }
            fileToReplicas.put(file.getFileName(), replicas);
        }
        return new WriteMsg(lastTransaction++, timestamp, primaryReplicas.get(file.getFileName()));
    }

    public ArrayList<ReplicaLoc> getReplicas(String fileName){
        return fileToReplicas.get(fileName);

    }
}
