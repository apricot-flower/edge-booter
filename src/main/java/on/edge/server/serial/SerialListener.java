package on.edge.server.serial;

import on.edge.config.ServerItems;
import on.edge.server.ServerContext;

import java.util.*;

/**
 * 串口主站
 */
public class SerialListener implements ServerContext {


    private final Map<String, SerialAdministrators> serials = new HashMap<>();

    public SerialListener(List<ServerItems> items) {
        for (ServerItems item : items) {
            serials.put(item.getName(), new SerialAdministrators(item));
        }
    }

    /**
     * 启动
     */
    @Override
    public SerialListener start() throws Exception {
        Set<String> keys = serials.keySet();
        for (String key : keys) {
            serials.get(key).start();
        }
        return this;
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws Exception {
        Collection<SerialAdministrators> collections = serials.values();
        for (SerialAdministrators s : collections) {
            s.close();
        }
    }

    @Override
    public void handleException(Throwable ex) {
    }

    public SerialAdministrators selectSerialLinker(String name) {
        return serials.getOrDefault(name, null);
    }
}
