# edge-booter
边设备使用类springboot框架

#### 导包
```xml
<dependency>
    <groupId>on.edge</groupId>
    <artifactId>edge-booter</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 如果使用该框架，你的项目的build应类似于如下
```xml
<build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.3.0</version>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <manifest>
                        <mainClass>
                            com.td.Application
                        </mainClass>
                    </manifest>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
    <resources>
        <resource>
            <directory>${project.basedir}/src/main/resources</directory>
            <filtering>false</filtering>
            <includes>
                <include>application.yml</include>
                <include>application-*.yml</include>
            </includes>
        </resource>
    </resources>
</build>
```
#### 架构
java1.8 + netty
### 配置文件（名称只能是application.yml）
```yaml
edge:
  server:
    - WEB_SERVER: #web服务端
        port: 8081
    - TCP_MASTER: #tcp服务端
        port: 8021
    - TCP_SLAVE: #tcp客户端
        - name: "master1"
          host: 127.0.0.1
          port: 10087
          timeout: 3 #连接时间， 单位秒
          reconnect: true #是否断线重连
        - name: "master2"
          host: 127.0.0.1
          port: 10089
          timeout: 3 #连接时间， 单位秒
          reconnect: true #是否断线重连
    - SERIAL: #串口服务器
        - name: "serial1"
          dev: "COM7"
          baud: 9600
          dataBits: 8
          stopBits: 1
          parity: 1
          timeout: 3 #连接时间， 单位秒

        - name: "serial2"
          dev: "COM7"
          baud: 9600
          dataBits: 8
          stopBits: 1
          parity: 1
          timeout: 3 #连接时间， 单位秒
```
### WEB服务器
###### 内置的统一返回类，可以不用自定义
```java
import on.edge.server.web.BaseUnifiedResponse;
```
###### 如何使用
```java
@ControllerPath("com.td.controller") //controller层位置
public class Application {

    public static void main(String[] args) throws Exception {
        try {
            EdgeApplication.run(Application.class, args);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```
###### 举例
```java
@Controller("/test")
public class TestController {

    @Mapping(method = HttpMethod.GET, uri = "/getMethod")
    public BaseUnifiedResponse<?> getMethod(@Param("arg0") int arg0, @Param("arg1") String arg1) {
        return BaseUnifiedResponse.success();
    }


    @Mapping(method = HttpMethod.POST, uri = "/postMethod")
    public BaseUnifiedResponse<?> postMethod(@Param("arg0") int arg0, @Param("arg1") String arg1) {
        return BaseUnifiedResponse.error();
    }



    @Mapping(method = HttpMethod.POST, uri = "/postMethodObject")
    public BaseUnifiedResponse<?> postMethodObject(@Param("tt") TT tt, @Param("name") String name) {
        return BaseUnifiedResponse.success(new ArrayList<>());
    }

}
```
###### IOC
```yaml
import com.td.controller.wq.TT;
import on.edge.ioc.Bean;
import on.edge.ioc.Component;
import on.edge.ioc.Resource;
import on.edge.ioc.Value;

@Component(name = "tt_config")
public class TTConfig {

    /**
     * 获取yml中的数据
     */
    @Value(name = "ed.ge.ss")
    private String todos;

    /**
     * 自动加载一个对象
     */
    @Resource(name = "first_tt")
    private TT ts;

    @Bean(name = "second_tt")
    public TT buildTT() {
        return new TT("test", 22);
    }
}
```

###### WEB服务器全局异常管理
1. 实现on.edge.server.web.GlobalExceptionHandler
2. 启动项加载全局异常
```java
@ControllerPath("com.td.controller") //controller层位置
public class Application {

    public static void main(String[] args) throws Exception {
        try {
            EdgeApplication.build(Application.class, args).buildWebGlobalErrorHandle(new MyGlobalExceptionHandler()).run();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
```

###### 操作数据库(edge-booter是用来给边端用的，对于本地数据库操作的需求并不大，所以并没有实现连接池)
1. 配置文件
```yaml
edge:
  jdbc:
    driver: "org.sqlite.JDBC" #驱动
    url: "jdbc:sqlite:C:\\Users\\dev\\Desktop\\43\\cac.db3"
    username:
    password:
    location: "C:\\Users\\dev\\IdeaProjects\\ed-test\\jdbc" #sql语句文件位置
```
2. 配置文件（sql语句文件）为了方便，规定所有的sql配置文件都必须以"edge_"开头,否则无法解析
```xml
<?xml version="1.0" encoding="UTF-8"?>
<orms table_name="person">
    <orm name="insert_person" operate="insert" sql="INSERT INTO person (name, age) VALUES (${name}, ${age})"/>
    <orm name="delete_person" operate="delete" sql="delete from person where name=${name}"/>
    <orm name="update_person" operate="update" sql="UPDATE person SET name = ${name} where age=${age}"/>
    <orm name="select_all_person" operate="select" sql="select * from person;" resultType="com.td.controller.wq.TT" />
    <orm name="select_all_person_by_name" operate="select" sql="select * from person where name = ${name} and age=${name};" resultType="com.td.controller.wq.TT" />
</orms>
```

3. 使用教程

3.1 注入
```text
 @Resource(name = "person")
 private JDBCManager jdbcManager;
```
3.2 使用方法1
```text
 Object value = jdbcManager.operate("select_all_person_by_name")
                .append("name", "apricot111")
                .append("age", 10000)
                .load();
```
3.3 使用方法2
```text
Map<String, Object> map = new HashMap<>();
        map.put("name", "apricot111");
        map.put("age", 10000);
        Object value = jdbcManager.operate("select_all_person_by_name")
                .addParams(map)
                .load();
```
### TCP服务端
###### 实现处理类
```java
package com.td.config;

import io.netty.buffer.ByteBuf;
import on.edge.server.tcp_master.TCPChannelManager;

import java.nio.ByteBuffer;

public class MyTCPChannelManager extends TCPMasterChannelManager {


    /**
     * 收到消息
     * @param clientIdent 客户端/从站标识
     * @param msg 收到的消息
     */
    @Override
    public void receivedMessage(String clientIdent, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer nioBuffer = byteBuf.nioBuffer();

        // 获取所有数据到一个字节数组
        byte[] allData = new byte[nioBuffer.remaining()];
        nioBuffer.get(allData);
        System.out.println("收到消息：" + new String(allData));
    }

    @Override
    public void clientDisconnect(String s) {
        System.out.println("断开链接：" + s);
    }

    @Override
    public void clientConnected(String s, String s1) {
        System.out.println("新连接：" + s);
    }

    @Override
    public void exceptionCaught(String s, Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void run() {
        //一些业务
        System.out.println("tcp_master已经启动， 你可以在这个方法中进行一些操作， 该方法只会运行一次~~~");
    }
}
```
###### 创建解码器(如果业务不需要解码器，就可以不创建)
1. 创建基于分隔符处理数据帧
```java
MyTCPChannelManager tcm = new MyTCPChannelManager().buildSeparator(分隔符, 报文最大长度);
```

2. 创建基于报文长度处理数据帧
```java
MyTCPChannelManager tcm = new MyTCPChannelManager().buildFixedLength(基于报文长度处理数据帧);
```

3. 创建处理包含长度字段的消息帧
```java
MyTCPChannelManager tcm = new MyTCPChannelManager().buildLengthFieldBasedFrame(指定长度字段的字节序, 允许的最大帧长度, 长度字段在消息中的偏移量,长度字段的长度,长度字段值的调整量,从解码后的帧中剥离的字节数,是否抛出异常);
```

4.自定义解码器(举例：创建按照指定分割符分割的解码器)
```java
public class MyDecodeManager implements ChannelManager {
    @Override
    public ByteToMessageDecoder build() throws Exception {
        ByteBuf buf = Unpooled.copiedBuffer("21".getBytes());
        return new DelimiterBasedFrameDecoder(1024, buf);
    }
}
```

###### 使用
```java
@ControllerPath("com.td.controller") //controller层位置
public class Application {

    public static void main(String[] args) throws Exception {
        try {
            EdgeApplication.build(Application.class, args).buildTcpMasterChannelManager(new MyTCPChannelManager().buildCustomize(new MyDecodeManager()))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### TCP客户端

###### 实现处理类

```java
package com.td.tcpslave;

import io.netty.buffer.ByteBuf;
import on.edge.server.tcp_slave.TCPSlaveChannelManager;

import java.nio.ByteBuffer;

public class Master1TcpSlaveManager extends TCPSlaveChannelManager {
    @Override
    public void receivedMessage(Object msg, String name, String host, int port) {
        ByteBuf byteBuf = (ByteBuf) msg;
        ByteBuffer nioBuffer = byteBuf.nioBuffer();

        // 获取所有数据到一个字节数组
        byte[] allData = new byte[nioBuffer.remaining()];
        nioBuffer.get(allData);
        System.out.println("收到消息：" + new String(allData));
    }

    @Override
    public void connected(String name, String host, int port, String address) {
        System.out.println("===============和主站建立连接成功================");
        System.out.println("连接名称：" + name);
        System.out.println("主站host：" + host);
        System.out.println("主站端口：" + port);
        System.out.println("本机地址：" + address);
        System.out.println("=============================================");
    }

    @Override
    public void disConnected(String name, String host, int port, String address, boolean reconnect) {
        System.out.println("===============和主站断开连接================");
        System.out.println("连接名称：" + name);
        System.out.println("主站host：" + host);
        System.out.println("主站端口：" + port);
        System.out.println("本机地址：" + address);
        System.out.println("是否会自动重连：" + reconnect);
        System.out.println("=============================================");
    }

    @Override
    public void exceptionCaught(Throwable cause, String name, String host, int port, String address) {
        System.out.println("发生异常");
    }

    @Override
    public void run() {
        System.out.println("tcp_slave1已经启动， 你可以在这个方法中进行一些操作， 该方法只会运行一次~~~");
    }
}

```

###### 创建解码器(如果业务不需要解码器，就可以不创建)
同tcp服务端

###### 使用
```java
@ControllerPath("com.td.controller") //controller层位置
public class Application {

    //https://www.cnblogs.com/tanghaorong/p/12314070.html
    public static void main(String[] args) throws Exception {
        try {
            EdgeApplication.build(Application.class, args).buildTcpSlaveChannelManager("master1", new Master1TcpSlaveManager().buildSeparator("21", 1024))
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
### 串口
###### 获取指定名称的串口连接器，连接器中有读/写/刷新串口功能
```java
EdgeApplication.selectSerialLinker("serial1")
```