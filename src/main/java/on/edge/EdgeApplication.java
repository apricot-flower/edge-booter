package on.edge;


import on.edge.ioc.EdgeIocListener;
import on.edge.server.serial.SerialAdministrators;
import on.edge.server.serial.SerialListener;
import on.edge.server.tcp_master.TCPMasterChannelManager;
import on.edge.server.tcp_master.TCPMasterListener;
import on.edge.server.tcp_slave.TCPSlaveChannelManager;
import on.edge.server.tcp_slave.TCPSlaveListener;
import on.edge.server.web.GlobalExceptionHandler;
import on.edge.server.web.WebServerListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 主启动类
 */
@SuppressWarnings("all")
public class EdgeApplication {

    private static EdgeApplication edge;

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
     * TCP slave
     */
    private TCPSlaveListener tcpSlaveListener;

    /**
     * web全局异常
     */
    private GlobalExceptionHandler globalExceptionHandler;

    /**
     * tcp主站处理器
     */
    private TCPMasterChannelManager tcpChannelManager;

    /**
     * 串口处理器
     */
    private SerialListener serialListener;

    /**
     * tcp 客户端处理器
     */
    private Map<String, TCPSlaveChannelManager> tcpSlaveChannelManagers;

    public EdgeApplication(Class<?> param, String... args) throws Exception {
        this.tcpSlaveChannelManagers = new HashMap<>();
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
        this.tcpSlaveListener.close();
        this.serialListener.close();
    }

    //构建主服务
    private void serverBuild() throws Exception {
        //构建web服务
        if (this.configListener.checkServer(SERVER.WEB)) {
            this.webServerListener = new WebServerListener(this.configListener.getEdgeConfig().getServer().getWebServer().getPort(), param).buildWebGlobalErrorHandle(globalExceptionHandler).build(this.edgeIocListener.getIoc(),this.configListener.getAllConfigs()).start();
        }
        if (this.configListener.checkServer(SERVER.TCP_MASTER)) {
            this.tcpMasterListener = new TCPMasterListener(this.configListener.getEdgeConfig().getServer().getTcpMaster().getPort(), this.tcpChannelManager).start();
        }
        if (this.configListener.checkServer(SERVER.TCP_SLAVE)) {
            this.tcpSlaveListener = new TCPSlaveListener(this.configListener.getEdgeConfig().getServer().getTcpSlave(), this.tcpSlaveChannelManagers).start();
        }
        if (this.configListener.checkServer(SERVER.SERIAL)) {
            this.serialListener = new SerialListener(this.configListener.getEdgeConfig().getServer().getSerial()).start();
        }
        latch.await();

    }

    public static EdgeApplication build(Class<?> param, String... args) throws Exception {
        edge = new EdgeApplication(param, args);
        return edge;
    }

    public static EdgeApplication run(Class<?> param, String... args) throws Exception {
        edge = new EdgeApplication(param, args).run();
        return edge;
    }


    /**
     * 全局异常处理
     */
    public EdgeApplication buildWebGlobalErrorHandle(GlobalExceptionHandler handler) {
        this.globalExceptionHandler = handler;
        return this;
    }


    /**
     * tcp主站的处理器
     */
    public EdgeApplication buildTcpMasterChannelManager(TCPMasterChannelManager tcpChannelManager) {
        this.tcpChannelManager = tcpChannelManager;
        return this;
    }

    /**
     * TCP 客户端的处理器
     */
    public EdgeApplication buildTcpSlaveChannelManager(String tcpDriver, TCPSlaveChannelManager tcpSlaveChannelManager) {
        this.tcpSlaveChannelManagers.put(tcpDriver, tcpSlaveChannelManager);
        return this;
    }

    public EdgeApplication run() throws Exception {
        serverBuild();
        return this;
    }

    protected SerialListener getSetialLinster() {
        return this.serialListener;
    }


    /**
     * 获取指定名称的串口
     */
    public static SerialAdministrators selectSerialLinker(String name) {
        return edge.getSetialLinster().selectSerialLinker(name);
    }
}
