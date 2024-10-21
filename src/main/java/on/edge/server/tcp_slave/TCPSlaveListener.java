package on.edge.server.tcp_slave;

import on.edge.config.ServerItems;
import on.edge.server.ServerContext;

import java.util.*;

/**
 * TCP客户端
 */
@SuppressWarnings("all")
public class TCPSlaveListener implements ServerContext {

    private final Map<String, TCPSlaveAdministrators> slaves;

    private static final Map<String, Boolean> linkFlags = Collections.synchronizedMap(new HashMap<>());



    public TCPSlaveListener(List<ServerItems> slaveItemsList, Map<String, TCPSlaveChannelManager> tcpSlaveChannelManagers) {
        this.slaves = new HashMap<>();
        int size = slaveItemsList.size();
        for (ServerItems items : slaveItemsList) {
            this.slaves.put(items.getName(), new TCPSlaveAdministrators(items, tcpSlaveChannelManagers.getOrDefault(items.getName(), null)));
        }
    }

    public static void linked(String name, boolean linkFlag) {
        linkFlags.put(name, linkFlag);
    }

    public static boolean linkedFlag(String name) {
        return linkFlags.getOrDefault(name, false);
    }

    /**
     * 启动
     */
    @Override
    public TCPSlaveListener start() throws Exception {
        Collection<TCPSlaveAdministrators> collection = slaves.values();
        for (TCPSlaveAdministrators slave : collection) {
            slave.start();
        }
        return this;
    }

    /**
     * 关闭
     */
    @Override
    public void close() throws Exception {
        Collection<TCPSlaveAdministrators> collection = slaves.values();
        for (TCPSlaveAdministrators slave : collection) {
            slave.close();
        }
    }

    @Override
    public void handleException(Throwable ex) {
        ex.printStackTrace();
    }
}
