package on.edge.except;

public class SerialException extends RuntimeException {
    public SerialException() {
        super("serial error!");
    }

    public SerialException(String message) {
        super(message);
    }
}
