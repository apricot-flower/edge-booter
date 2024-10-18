package on.edge.config;

/**
 * 数据库连接配置
 */
public class JDBCConfig {

    private String driver;


    private String url;

    private String username;

    private String password;

    private String location;

    public JDBCConfig() {
    }

    public JDBCConfig(String driver, String url, String username, String password, String location) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.location = location;
    }


    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
