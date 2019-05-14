package replica;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileHandler {
    private ReadWriteLock rwLock;
    private FileWriter writer;
    private File file;

    public FileHandler(String fileName) throws IOException {
        writer = new FileWriter(fileName, true);
        file = new File(fileName);
        rwLock = new ReentrantReadWriteLock();
    }

    private ReadWriteLock getLock() {
        return rwLock;
    }

    public String read(int rid) throws IOException {
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes);
    }

    public void write(String data) throws IOException {
        writer.write(data);
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void close() throws IOException {
        writer.close();
    }
}
