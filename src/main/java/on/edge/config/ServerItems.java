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

    private String parity;


    public ServerItems() {
    }


    public ServerItems(String type, String name, int port, String host, String dev, int baud, int dataBits, int stopBits, String parity) {
        this.type = type;
        this.name = name;
        this.port = port;
        this.host = host;
        this.dev = dev;
        this.baud = baud;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getParity() {
        return parity;
    }



    public void setParity(String parity) {
        this.parity = parity;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ServerItems{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", port=" + port +
                ", host='" + host + '\'' +
                ", dev='" + dev + '\'' +
                ", baud=" + baud +
                ", dataBits=" + dataBits +
                ", stopBits=" + stopBits +
                ", parity='" + parity + '\'' +
                '}';
    }
}
