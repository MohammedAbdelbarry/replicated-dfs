package replica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileHandler {
    private ReadWriteLock rwLock;
    private FileWriter writer;
    private File file;
    private Path tempPath;

    public FileHandler(String fileName) throws IOException {
        file = new File(fileName);
        rwLock = new ReentrantReadWriteLock();
        prepareTempFile(fileName);
    }

    private void prepareTempFile(String fileName) throws IOException {
        int key = new Random().nextInt();
        tempPath = Files.createTempFile(fileName.substring(fileName.lastIndexOf(File.separator) + 1), "tmp" + key);
        if (file.exists()) {
            Files.copy(file.toPath(), tempPath, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Temp(" + file.getPath() + ") = " + tempPath);
        writer = new FileWriter(tempPath.toString(), true);
    }

    public ReadWriteLock getLock() {
        return rwLock;
    }

    public String read() throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes);
    }

    public void write(String data) throws IOException {
        writer.write(data);
    }

    public void flush() throws IOException {
        writer.flush();
        writer.close();
        Files.copy(tempPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.delete(tempPath);
        prepareTempFile(file.getName());
    }

    public void close() throws IOException {
        writer.close();
    }
}
