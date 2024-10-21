package on.edge.server.serial;

import com.fazecast.jSerialComm.SerialPort;
import on.edge.config.ServerItems;
import on.edge.except.SerialException;
import on.edge.server.ServerContext;


/**
 * 串口连接
 */
@SuppressWarnings("all")
public class SerialAdministrators implements ServerContext {

    private String name;

    private String dev;

    private int baud;

    private int dataBits;

    private int stopBits;

    private int parity;

    private int timeout;

    private SerialPort serialPort;

    public SerialAdministrators(ServerItems item) {
        this.name = item.getName();
        this.dev = item.getDev();
        this.baud = item.getBaud();
        this.dataBits = item.getDataBits();
        this.stopBits = item.getStopBits();
        this.parity = item.getParity();
        this.timeout = item.getTimeout();
    }

    /**
     * 启动
     */
    @Override
    public final ServerContext start() throws Exception {
        serialPort = SerialPort.getCommPort(this.dev);
        serialPort.setComPortParameters(this.baud, this.dataBits, this.stopBits, this.parity); // 设置端口参数
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, this.timeout * 1000, this.timeout * 1000);
        boolean openFlg = serialPort.openPort();
        if (!openFlg) {
            throw new SerialException(this.name + " <" + this.dev + "> connect error!");
        }
        return this;
    }


    /**
     * 关闭
     */
    @Override
    public final void close() throws Exception {
        this.serialPort.closePort();
    }

    @Override
    public final void handleException(Throwable ex) {
        ex.printStackTrace();
        System.exit(1);
    }


    /**
     * 刷新串口
     */
    public final void flush() throws Exception {
        this.serialPort.clearDTR();
        this.serialPort.clearRTS();
    }

    /**
     * 写数据
     */
    public final void write(byte[] data) {
        serialPort.writeBytes(data, data.length);
    }

    /**
     * 读数据
     * @param length 要读的数据长度
     * @return
     */
    public final byte[] read(int length) {
        byte[] result = new byte[length];
        int size = serialPort.readBytes(result, result.length);
        return result;
    }

    public String getName() {
        return name;
    }

    public String getDev() {
        return dev;
    }

    public int getBaud() {
        return baud;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    public int getTimeout() {
        return timeout;
    }

}
