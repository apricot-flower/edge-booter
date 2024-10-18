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
    - WEB_SERVER:
        port: 8081
    - TCP_MASTER:
        port: 8021
    - TCP_SLAVE:
        - name: "test1"
          host: 127.0.0.1
          port: 10087
        - name: "test2"
          host: 127.0.0.1
          port: 10089
    - SERIAL:
        - name: "serial1"
          dev: "COM7"
          baud: 9600
          dataBits: 8
          stopBits: 1
          parity: "E"

        - name: "serial2"
          dev: "COM7"
          baud: 9600
          dataBits: 8
          stopBits: 1
          parity: "E"
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
