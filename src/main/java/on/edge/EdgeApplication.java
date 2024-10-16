package on.edge;

import on.edge.ioc.WebIocListener;
import on.edge.server.web.WebServerListener;

//主启动类
@SuppressWarnings("all")
public class EdgeApplication {

    private Class<?> param;

    private ConfigListener configListener;

    /**
     * IOC
     */
    private WebIocListener webIocListener;

    /**
     * web服务
     */
    private WebServerListener webServerListener;


    public EdgeApplication(Class<?> param, String... args) throws Exception {
        this.param = param;
        this.configListener = new ConfigListener(args, param.getClassLoader()).build();
        this.webIocListener = new WebIocListener(param, configListener.getAllConfigs()).build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                this.shutDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    private void shutDown() throws Exception {
        this.webServerListener.close();
    }

    //构建主服务
    private void serverBuild() throws Exception {
        //构建web服务
        if (this.configListener.checkServer(SERVER.WEB)) {
            this.webServerListener = new WebServerListener(this.configListener.getEdgeConfig().getServer().getWebServer().getPort(), param).build();
            this.webServerListener.start();
        }
    }

    public static EdgeApplication run(Class<?> param, String... args) throws Exception {
        EdgeApplication edgeApplication = new EdgeApplication(param, args);
        edgeApplication.serverBuild();
        return edgeApplication;
    }



}
