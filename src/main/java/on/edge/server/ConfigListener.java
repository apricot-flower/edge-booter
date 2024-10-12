package on.edge.server;

import on.edge.except.ConfigException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * 配置文件
 */
public class ConfigListener {

    private static final Logger logger = LogManager.getLogger(ConfigListener.class);


    private final Map<String, String> argsMap;

    private final Map<String, ServerListener> serverMap;

    private final Map<String, Integer> portMap;


    public ConfigListener(String[] args, ClassLoader classLoader) {
        this.argsMap = new HashMap<>();
        for (String arg : args) {
            String[] argArray = arg.split("=", 2);
            this.argsMap.put(argArray[0], argArray[1]);
        }
        //解析yaml位置
        Yaml yaml = new Yaml();
        Map<String, Object> map;
        if (this.argsMap.containsKey("--edge.configuration")) {
            map = yaml.load(this.argsMap.get("--edge.configuration"));
        } else {
            //配置
            map = yaml.load(classLoader.getResourceAsStream("application.yml"));
        }
        if (!map.containsKey("edge")) {
            throw new ConfigException("No server type configured！");
        }
        this.serverMap = new HashMap<>();
        this.portMap = new HashMap<>();
        List<Map<String, Object>> servers = (List<Map<String, Object>>) map.get("edge");
        for (Map<String, Object> server : servers) {
            Set<String> serverTypes = server.keySet();
            for (String serverType : serverTypes) {
                buildServer(serverType, server.get(serverType));
            }
        }
    }

    private void buildServer(String serverType, Object items) {
        switch (serverType.trim().toUpperCase()) {
            case SERVER.WEB:
                buildWebServer(items);
                break;
            case SERVER.TCP_MASTER:
                buildTcpMasterServer(items);
                break;
            case SERVER.TCP_SLAVE:
                buildTcpSlaveServer(items);
                break;
            case SERVER.SERIAL:
                buildSerialServer(items);
                break;
            default:
                throw new ConfigException("config error!");
        }
    }

    private void buildSerialServer(Object items) {
        logger.debug("---->{}", items.toString());
        List<Map<String, String>> serials = (List<Map<String, String>>) items;
        for (Map<String, String> serial : serials) {
        }
    }

    private void buildTcpSlaveServer(Object items) {
        List<Map<String, String>> masters = (List<Map<String, String>>) items;
        Set<String> hosts = new HashSet<>();
        for (Map<String, String> item : masters) {

            logger.debug("----------> {}", item.toString());
            String name = item.get("name");
            if (name.trim().equals("")) {
                throw new ConfigException("tcp_slave must has name!");
            }
            String host = item.get("host").trim();
            int port = Integer.parseInt(String.valueOf(item.get("port")).trim());
            String hostFlag = host + ":" + port;
            if (hosts.contains(hostFlag)) {
                throw new ConfigException("tcp_slave host and port repeat!");
            } else {
                hosts.add(hostFlag);
            }
            String serverFlag = SERVER.TCP_SLAVE + "_" + name;
            ServerListener serverListener = new ServerListener();
            serverListener.setName(name);
            serverListener.setType(SERVER.TCP_SLAVE);
            serverListener.setHost(host);
            serverListener.setPort(port);
            this.serverMap.put(serverFlag, serverListener);
        }
    }

    private void buildTcpMasterServer(Object items) {
        Map<String, Integer> map = (Map<String, Integer>) items;
        Integer port = map.getOrDefault("port", SERVER.DEFAULT_TCP_MASTER_PORT);
        if (this.portMap.containsKey(SERVER.WEB)) {
            if (port.equals(this.portMap.get(SERVER.WEB))) {
                throw new ConfigException("WEB‘s port equals TCP_MASTER‘s port ");
            }
        } else {
            this.portMap.put(SERVER.TCP_MASTER, port);
        }
        ServerListener serverListener = new ServerListener();
        serverListener.setType(SERVER.TCP_MASTER);
        serverListener.setName(SERVER.TCP_MASTER);
        serverListener.setPort(port);
        this.serverMap.put(SERVER.TCP_MASTER, serverListener);
        logger.debug("tcp_master_server port: {}", port);
    }

    private void buildWebServer(Object items) {
        Map<String, Integer> map = (Map<String, Integer>) items;
        Integer port = map.getOrDefault("port", SERVER.DEFAULT_WEB_PORT);
        if (this.portMap.containsKey(SERVER.TCP_MASTER)) {
            if (Objects.equals(this.portMap.get(SERVER.TCP_MASTER), port)) {
                throw new ConfigException("WEB‘s port equals TCP_MASTER‘s port ");
            }
        } else {
            this.portMap.put(SERVER.WEB, port);
        }
        ServerListener serverListener = new ServerListener();
        serverListener.setType(SERVER.WEB);
        serverListener.setName(SERVER.WEB);
        serverListener.setPort(port);
        this.serverMap.put(SERVER.WEB, serverListener);
        logger.debug("web_server port: {}", port);
    }

}
