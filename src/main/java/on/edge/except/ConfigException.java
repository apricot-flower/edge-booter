package on.edge.except;

/**
 * application.yml文件错误
 */
public class ConfigException extends RuntimeException {

    public ConfigException() {
        super("bad configuration file!");
    }

    public ConfigException(String message) {
        super(message);
    }
}
