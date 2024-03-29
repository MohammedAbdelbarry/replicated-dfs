package replica;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileHandlerPool {
    private Map<String, FileHandler> handlerMap;

    private FileHandlerPool() {
        handlerMap = new HashMap<>();
    }

    public FileHandler getHandler(String fileName) throws IOException {
        if (!handlerMap.containsKey(fileName)) {
            handlerMap.putIfAbsent(fileName, new FileHandler(fileName));
        }
        return handlerMap.get(fileName);
    }

    public static FileHandlerPool getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final FileHandlerPool INSTANCE = new FileHandlerPool();
    }
}
