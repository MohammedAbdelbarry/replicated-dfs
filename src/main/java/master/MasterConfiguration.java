package master;

import java.io.*;
import java.util.Properties;

public class MasterConfiguration {
    private static final String RMI_PORT_VALUE = "0";
    private static final String RMI_PORT_KEY = "rmi.port";
    private int rmiPort;

    public MasterConfiguration(final String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            Properties properties = new Properties();
            properties.setProperty(RMI_PORT_KEY, RMI_PORT_VALUE);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getRmiPort() {
        return rmiPort;
    }
}
