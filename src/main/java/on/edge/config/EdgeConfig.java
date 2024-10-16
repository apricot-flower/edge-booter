package on.edge.config;

/**
 * 主配置文件
 */
public class EdgeConfig {

    private ServerConfig server;

    public EdgeConfig() {
        this.server = new ServerConfig();
    }


    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }
}
