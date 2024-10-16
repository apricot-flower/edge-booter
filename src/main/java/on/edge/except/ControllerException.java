package on.edge.except;

public class ControllerException extends RuntimeException {
    public ControllerException() {
        super("Repeat URI!");
    }

    public ControllerException(String message) {
        super(message);
    }
}
