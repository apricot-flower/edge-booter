package on.edge.config;

/**
 * 服务类型
 */
public class ServerItems {

    private String type;

    private String name;

    private int port;

    private String host;

    private String dev;

    private int baud;

    private int dataBits;

    private int stopBits;

    private int parity;

    private int timeout;

    private boolean reconnect;

    public ServerItems() {
    }

    public ServerItems(String type, String name, int port, String host, String dev, int baud, int dataBits, int stopBits, int parity, int timeout, boolean reconnect) {
        this.type = type;
        this.name = name;
        this.port = port;
        this.host = host;
        this.dev = dev;
        this.baud = baud;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
        this.timeout = timeout;
        this.reconnect = reconnect;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public int getBaud() {
        return baud;
    }

    public void setBaud(int baud) {
        this.baud = baud;
    }

    public int getDataBits() {
        return dataBits;
    }

    public void setDataBits(int dataBits) {
        this.dataBits = dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public void setStopBits(int stopBits) {
        this.stopBits = stopBits;
    }

    public int getParity() {
        return parity;
    }

    public void setParity(int parity) {
        this.parity = parity;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }


}
