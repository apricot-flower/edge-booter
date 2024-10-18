package on.edge.server;

/**
 * 服务接口
 */
public interface ServerContext {

    /**
     * 启动
     */
    ServerContext start() throws Exception;

    /**
     * 关闭
     */
    void close() throws Exception;

    void handleException(Throwable ex);

}
