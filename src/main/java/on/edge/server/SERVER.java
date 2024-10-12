package on.edge.server;


/**
 * 服务类型。 0-web， 1-tcp_master, 2-tcp_slave 3-serial
 */
public interface SERVER {
    public static final String WEB = "WEB_SERVER";
    public static final int DEFAULT_WEB_PORT = 8080;

    public static final String TCP_MASTER = "TCP_MASTER";
    public static final int DEFAULT_TCP_MASTER_PORT = 8314;

    public static final String TCP_SLAVE = "TCP_SLAVE";
    public static final String SERIAL = "SERIAL";
    ;


}
