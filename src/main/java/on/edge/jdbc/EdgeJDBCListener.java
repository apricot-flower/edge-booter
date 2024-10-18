package on.edge.jdbc;

import on.edge.config.JDBCConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * JDBC
 */
@SuppressWarnings("all")
public class EdgeJDBCListener {



    private static final String SELECT = "select";

    private String driver;
    private String url;
    private String username;
    private String password;
    private Connection conn;
    private Statement statement;

    public EdgeJDBCListener(JDBCConfig jdbcConfig) throws Exception {
        this.driver = jdbcConfig.getDriver();
        this.url = jdbcConfig.getUrl();
        this.username = jdbcConfig.getUsername();
        this.password = jdbcConfig.getPassword();
        Class.forName(this.driver);
        Connection conn = DriverManager.getConnection(this.url, this.username, this.password);
        this.statement = conn.createStatement();
    }


    public void close() throws Exception {
        this.conn.close();
    }

    public synchronized Object load(String sqlFrame, String operate) throws Exception {
        if (operate != null && operate.equals(SELECT)) {
            return this.statement.executeQuery(sqlFrame);
        }
        return this.statement.executeUpdate(sqlFrame);
    }
}
