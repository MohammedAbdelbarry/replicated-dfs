package master;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public class MasterServer implements MasterServerClientInterface {
    private HashMap<String, ReplicaLoc> primaryReplicas;
    private ArrayList<ReplicaLoc> replicaServers;
    private long lastTransaction;

    public MasterServer(final String configFilePath, final String replicaFilePath){
        primaryReplicas = new HashMap<>();
        replicaServers = new ArrayList<>();
        this.lastTransaction = -1;
        try {
            BufferedReader bufferReader = new BufferedReader(new FileReader(replicaFilePath));
            String line;
            while ((line = bufferReader.readLine()) != null) {
                String[] tokens = line.split(" ");
                replicaServers.add(new ReplicaLoc(tokens[0], tokens[1]));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ReplicaLoc[] read(String fileName) throws FileNotFoundException,
            IOException, RemoteException {
        if(!primaryReplicas.containsKey(fileName)){
            throw new FileNotFoundException();
        }
        ReplicaLoc replicaLoc = primaryReplicas.get(fileName);
        // call primary replica to check if file exists replicaLoc.getIp();
        boolean fileExists = true;
        if(!fileExists){
            throw new FileNotFoundException();
        }
        return replicaServers.toArray(new ReplicaLoc[replicaServers.size()]);
    }

    public WriteMsg write(FileContent file) throws RemoteException, IOException {
        Date date = new Date();
        long timestamp = date.getTime();
        if(!primaryReplicas.containsKey(file.getFileName())){
            Random rand = new Random();
            int randIdx = rand.nextInt(replicaServers.size());
            primaryReplicas.put(file.getFileName(), replicaServers.get(randIdx));
        }
        return new WriteMsg(lastTransaction++, timestamp, primaryReplicas.get(file.getFileName()));
    }
}
