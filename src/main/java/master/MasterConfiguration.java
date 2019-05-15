package master;

import java.io.*;
import java.util.Properties;

public class MasterConfiguration {
    private static final String RMI_PORT_KEY = "rmi.port";
    private static final String RMI_PORT_VALUE = "0";
    private static final String REPLICATION_FACTOR_KEY = "rmi.replication";
    private static final String REPLICATION_FACTOR_VALUE = "3";
    private static final String RMI_KEY_KEY = "rmi.key";
    private static final String RMI_KEY_VALUE = "master";
    private static final String REPLICAS_FILEPATH_KEY = "rm.replicas.filepath";
    private static final String REPLICAS_FILEPATH_VALUE = "replicas.properties";

    private int rmiPort;
    private String rmiKey;
    private int replicationFactor;
    private String replicasFilePath;

    public MasterConfiguration(final String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Properties properties = new Properties();
            properties.setProperty(RMI_PORT_KEY, RMI_PORT_VALUE);
            properties.setProperty(RMI_KEY_KEY, RMI_KEY_VALUE);
            properties.setProperty(REPLICATION_FACTOR_KEY, REPLICATION_FACTOR_VALUE);
            properties.setProperty(REPLICAS_FILEPATH_KEY, REPLICAS_FILEPATH_VALUE);
            try (OutputStream outputStream = new FileOutputStream(file)) {
                properties.store(outputStream, "Master Server Configuration File");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(file)) {
            properties.load(inputStream);
            rmiPort = Integer.parseInt(properties.getProperty(RMI_PORT_KEY, RMI_PORT_VALUE));
            rmiKey = properties.getProperty(RMI_KEY_KEY, RMI_KEY_VALUE);
            replicationFactor = Integer.parseInt(properties.getProperty(REPLICATION_FACTOR_KEY, REPLICATION_FACTOR_VALUE));
            replicasFilePath = properties.getProperty(REPLICAS_FILEPATH_KEY, REPLICAS_FILEPATH_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRmiPort() {
        return rmiPort;
    }

    public String getRmiKey() {
        return rmiKey;
    }

    public int getReplicationFactor() {
        return replicationFactor;
    }

    public String getReplicasFilePath() {
        return replicasFilePath;
    }
}
