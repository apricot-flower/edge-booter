package on.edge.server.web;

/**
 * 全局异常处理
 */
public interface GlobalExceptionHandler<T> {

    public T handle(Throwable e);

}
