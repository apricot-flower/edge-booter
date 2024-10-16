package on.edge.config;


import on.edge.SERVER;

import java.util.List;

/**
 * 服务文件
 */
public class ServerConfig {

    private ServerItems webServer;

    private ServerItems tcpMaster;

    private List<ServerItems> tcpSlave;

    private List<ServerItems> serial;

    public ServerConfig() {
    }

    public ServerConfig(ServerItems webServer, ServerItems tcpMaster, List<ServerItems> tcpSlave, List<ServerItems> serial) {
        this.webServer = webServer;
        this.tcpMaster = tcpMaster;
        this.tcpSlave = tcpSlave;
        this.serial = serial;
    }

    public ServerItems getWebServer() {
        return webServer;
    }

    public void setWebServer(ServerItems webServer) {
        this.webServer = webServer;
    }

    public ServerItems getTcpMaster() {
        return tcpMaster;
    }

    public void setTcpMaster(ServerItems tcpMaster) {
        this.tcpMaster = tcpMaster;
    }

    public List<ServerItems> getTcpSlave() {
        return tcpSlave;
    }

    public void setTcpSlave(List<ServerItems> tcpSlave) {
        this.tcpSlave = tcpSlave;
    }

    public List<ServerItems> getSerial() {
        return serial;
    }

    public void setSerial(List<ServerItems> serial) {
        this.serial = serial;
    }

    @Override
    public String toString() {
        return "ServerConfig{" +
                "webServer=" + webServer +
                ", tcpMaster=" + tcpMaster +
                ", tcpSlave=" + tcpSlave +
                ", serial=" + serial +
                '}';
    }

    public boolean check(String serverType) {
        if (serverType.equals(SERVER.WEB)) {
            return webServer != null;
        } else if (serverType.equals(SERVER.TCP_MASTER)) {
            return tcpMaster != null;
        } else if (serverType.equals(SERVER.TCP_SLAVE)) {
            return tcpSlave != null && tcpSlave.size() > 0;
        } else if (serverType.equals(SERVER.SERIAL)) {
            return serial != null && serial.size() > 0;
        }
        return false;
    }
}
