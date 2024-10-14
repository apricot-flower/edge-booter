package on.edge;

import on.edge.server.ConfigListener;
import on.edge.server.SERVER;
import on.edge.server.web.WebServerListener;

//主启动类
public class EdgeApplication {

    private Class<?> param;

    private ConfigListener configListener;

    /**
     * web服务
     */
    private WebServerListener webServerListener;


    public EdgeApplication(Class<?> param, String... args) {
        this.param = param;
        this.configListener = new ConfigListener(args, param.getClassLoader());
        this.serverBuild();
    }

    //构建主服务
    private void serverBuild() {
        //构建web服务
        if (this.configListener.checkServer(SERVER.WEB)) {
            this.webServerListener = new WebServerListener(this.configListener.getEdgeConfig().getServer().getWebServer().getPort()).build(this.param);
            this.webServerListener.start();
        }
    }

    public static EdgeApplication run(Class<?> param, String... args) {
        return new EdgeApplication(param, args);
    }



}
