# edge-booter
边设备使用类springboot框架

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

###### 操作数据库


###### 登录