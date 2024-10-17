package on.edge.server.web;

/**
 * 服务接口
 */
public interface ServerContext {

    /**
     * 启动
     */
    public void start() throws Exception;

    /**
     * 关闭
     */
    public void close() throws Exception;

}
