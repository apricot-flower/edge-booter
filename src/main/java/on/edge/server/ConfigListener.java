package on.edge.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import on.edge.server.config.EdgeConfig;
import on.edge.server.config.ServerItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置文件
 */
public class ConfigListener {

    private static final Logger logger = LogManager.getLogger(ConfigListener.class);

    /**
     * args中配置文件的key
     */
    private static final String ymlKey = "--edge.configuration";
    private static final String ymlName = "application.yml";
    private static final String edgeKey = "edge";
    private static final String serverKey = "server";

    private final Map<String, String> argsMap = new HashMap<>();

    /**
     * 全配置文件
     */
    private JSONObject allConfigs;

    /**
     * 主配置文件
     */
    private final EdgeConfig edgeConfig = new EdgeConfig();


    public ConfigListener(String[] args, ClassLoader classLoader) {
        for (String arg : args) {
            String[] argArray = arg.split("=", 2);
            this.argsMap.put(argArray[0], argArray[1]);
        }
        Yaml yaml = new Yaml();
        Map<String, Object> configMap = new HashMap<>();
        try {
            if (this.argsMap.containsKey(ymlKey)) {
                configMap = yaml.load(this.argsMap.get(ymlKey));
            } else {
                //配置
                configMap = yaml.load(classLoader.getResourceAsStream(ymlName));
            }
        } catch (Exception e) {
            logger.error("Analyze main configuration error：", e);
            System.exit(1);
        }
        this.allConfigs = JSONObject.parseObject(JSONObject.toJSONString(configMap));
        if (this.allConfigs.containsKey(edgeKey)) {
            buildEdge();
        }
    }

    private void buildEdge() {
        JSONObject edge = this.allConfigs.getJSONObject(edgeKey);
        if (edge.containsKey(serverKey)) {
            //build server
            buildServer(edge.getJSONArray(serverKey));
        }
    }

    //解析server配置
    private void buildServer(JSONArray servers) {
        for (int index = 0; index < servers.size(); index ++) {
            JSONObject server = servers.getJSONObject(index);
            if (server.containsKey(SERVER.WEB)) {
                //解析Web服务
                JSONObject items = server.getJSONObject(SERVER.WEB);
                ServerItems webItems = JSONObject.parseObject(items.toJSONString(), ServerItems.class);
                this.edgeConfig.getServer().setWebServer(webItems);
            } else if (server.containsKey(SERVER.TCP_MASTER)) {
                JSONObject items = server.getJSONObject(SERVER.TCP_MASTER);
                ServerItems tcpMasterItems = JSONObject.parseObject(items.toJSONString(), ServerItems.class);
                this.edgeConfig.getServer().setTcpMaster(tcpMasterItems);
            } else if (server.containsKey(SERVER.TCP_SLAVE)) {
                JSONArray tcpSlaveItems = server.getJSONArray(SERVER.TCP_SLAVE);
                List<ServerItems> list = new ArrayList<>();
                for (int i = 0; i < tcpSlaveItems.size(); i ++) {
                    JSONObject tcpSlaveItem = tcpSlaveItems.getJSONObject(i);
                    ServerItems tsItems = JSONObject.parseObject(tcpSlaveItem.toJSONString(), ServerItems.class);
                    list.add(tsItems);
                }
                this.edgeConfig.getServer().setTcpSlave(list);
            } else if (server.containsKey(SERVER.SERIAL)) {
                JSONArray serialItems = server.getJSONArray(SERVER.SERIAL);
                List<ServerItems> serials = new ArrayList<>();
                for (int i = 0; i < serialItems.size(); i ++) {
                    JSONObject serialItem = serialItems.getJSONObject(i);
                    ServerItems tsItems = JSONObject.parseObject(serialItem.toJSONString(), ServerItems.class);
                    serials.add(tsItems);
                }
                this.edgeConfig.getServer().setSerial(serials);
            } else {
                logger.error("Undefined service types have appeared！");
            }
        }
    }


    public boolean checkServer(String serverType) {
        return this.edgeConfig.getServer().check(serverType);
    }

    public EdgeConfig getEdgeConfig() {
        return edgeConfig;
    }
}
