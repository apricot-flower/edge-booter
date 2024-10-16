package on.edge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import on.edge.config.EdgeConfig;
import on.edge.config.ServerItems;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
public class ConfigListener {

    /**
     * args中配置文件的key
     */
    private static final String ymlKey = "--edge.configuration";
    private static final String ymlName = "application.yml";
    private static final String edgeKey = "edge";
    private static final String serverKey = "server";
    private static final String argsSplit = "=";



    private final Map<String, String> argsMap;

    /**
     * 全体配置
     */
    private ObjectNode allConfigs;

    /**
     * 主配置文件
     */
    private final EdgeConfig edgeConfig;




    public ConfigListener(String[] args, ClassLoader classLoader) {
        this.argsMap = new HashMap<>();
        this.edgeConfig = new EdgeConfig();
        for (String arg : args) {
            String[] argArray = arg.split(argsSplit, 2);
            this.argsMap.put(argArray[0], argArray[1]);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> configMap;
        if (this.argsMap.containsKey(ymlKey)) {
            configMap = yaml.load(this.argsMap.get(ymlKey));
        } else {
            //配置
            configMap = yaml.load(classLoader.getResourceAsStream(ymlName));
        }
        this.allConfigs = new ObjectMapper().valueToTree(configMap);
    }


    public ConfigListener build() throws Exception {
        if (!this.allConfigs.has(edgeKey)) {
            return this;
        }
        JsonNode serverNode = this.allConfigs.get(edgeKey);
        if (!serverNode.has(serverKey)) {
            return this;
        }
        serverNode = serverNode.get(serverKey);
        for (int index = 0; index < serverNode.size(); index ++) {
            JsonNode servers = serverNode.get(index);
            if (servers.has(SERVER.WEB)) {
                this.edgeConfig.getServer().setWebServer(new ObjectMapper().treeToValue(servers.get(SERVER.WEB), ServerItems.class));
            }
            if (servers.has(SERVER.TCP_MASTER)) {
                this.edgeConfig.getServer().setTcpMaster(new ObjectMapper().treeToValue(servers.get(SERVER.TCP_MASTER), ServerItems.class));
            }
            if (servers.has(SERVER.TCP_SLAVE)) {
                this.edgeConfig.getServer().setTcpSlave(new ObjectMapper().treeToValue(servers.get(SERVER.TCP_SLAVE), List.class));
            }
            if (servers.has(SERVER.SERIAL)) {
                this.edgeConfig.getServer().setSerial(new ObjectMapper().treeToValue(servers.get(SERVER.SERIAL), List.class));
            }
        }
        return this;
    }


    public boolean checkServer(String serverType) {
        return this.edgeConfig.getServer().check(serverType);
    }


    public EdgeConfig getEdgeConfig() {
        return edgeConfig;
    }


    public ObjectNode getAllConfigs() {
        return allConfigs;
    }
}
