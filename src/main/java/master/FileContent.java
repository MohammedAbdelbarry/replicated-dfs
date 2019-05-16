package master;

import java.io.Serializable;

public class FileContent implements Serializable {

    private String fileName;
    private String data;
    private static final long serialVersionUID = 1L;

    public FileContent(String fileName, String data) {
        this.fileName = fileName;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "FileContent(" +
                "fileName='" + fileName + '\'' +
                ", data='" + data + '\'' +
                ')';
    }
}
