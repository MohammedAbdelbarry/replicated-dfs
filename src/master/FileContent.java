package master;

public class FileContent {

    private String fileName;
    private String data;

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
}
