package on.edge.except;

public class JDBCException extends RuntimeException {
    public JDBCException() {
        super("JDBC error!");
    }

    public JDBCException(String message) {
        super(message);
    }
}
