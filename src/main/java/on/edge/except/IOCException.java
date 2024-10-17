package on.edge.except;

/**
 * IOC错误
 */
public class IOCException extends RuntimeException {
    public IOCException() {
        super("Preload error!");
    }

    public IOCException(String message) {
        super(message);
    }
}
