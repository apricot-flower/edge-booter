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
###### 内置的统一返回类
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