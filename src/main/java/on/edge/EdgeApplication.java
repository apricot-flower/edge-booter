package on.edge;

import on.edge.ioc.EdgeIocListener;
import on.edge.server.tcp_master.TCPMasterListener;
import on.edge.server.web.GlobalExceptionHandler;
import on.edge.server.web.WebServerListener;

import java.util.concurrent.CountDownLatch;

/**
 * 主启动类
 */
@SuppressWarnings("all")
public class EdgeApplication {

    private static final CountDownLatch latch = new CountDownLatch(1);

    private Class<?> param;

    private ConfigListener configListener;

    /**
     * IOC
     */
    private EdgeIocListener edgeIocListener;

    /**
     * web服务
     */
    private WebServerListener webServerListener;

    /**
     * TCP master
     */
    private TCPMasterListener tcpMasterListener;

    /**
     * web全局异常
     */
    private GlobalExceptionHandler globalExceptionHandler;

    public EdgeApplication(Class<?> param, String... args) throws Exception {
        this.param = param;
        this.configListener = new ConfigListener(args, param.getClassLoader()).build();
        this.edgeIocListener = new EdgeIocListener(param, this.configListener.getAllConfigs()).buildORM(this.configListener.getEdgeConfig().getJdbcConfig()).build();
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
        this.tcpMasterListener.close();
    }

    //构建主服务
    private void serverBuild() throws Exception {
        //构建web服务
        if (this.configListener.checkServer(SERVER.WEB)) {
            this.webServerListener = new WebServerListener(this.configListener.getEdgeConfig().getServer().getWebServer().getPort(), param).buildWebGlobalErrorHandle(globalExceptionHandler).build(this.edgeIocListener.getIoc(),this.configListener.getAllConfigs()).start();
        }
        if (this.configListener.checkServer(SERVER.TCP_MASTER)) {
            this.tcpMasterListener = new TCPMasterListener(this.configListener.getEdgeConfig().getServer().getTcpMaster().getPort()).start();
        }


        latch.await();

    }

    public static EdgeApplication build(Class<?> param, String... args) throws Exception {
        EdgeApplication edgeApplication = new EdgeApplication(param, args);
        return edgeApplication;
    }

    public static EdgeApplication run(Class<?> param, String... args) throws Exception {
        EdgeApplication edgeApplication = new EdgeApplication(param, args).run();
        return edgeApplication;
    }


    /**
     * 全局异常处理
     */
    public EdgeApplication buildWebGlobalErrorHandle(GlobalExceptionHandler handler) {
        this.globalExceptionHandler = handler;
        return this;
    }

    public EdgeApplication run() throws Exception {
        serverBuild();
        return this;
    }

}
