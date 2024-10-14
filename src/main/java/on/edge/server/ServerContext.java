package on.edge.server;

/**
 * 服务接口
 */
public interface ServerContext {

    /**
     * 启动
     */
    public void start();

    /**
     * 关闭
     */
    public void close();
}
