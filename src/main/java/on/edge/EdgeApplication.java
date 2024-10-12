package on.edge;

import on.edge.server.ConfigListener;

//主启动类
public class EdgeApplication {

    private Class<?> param;

    private ConfigListener configListener;


    public EdgeApplication(Class<?> param, String... args) {
        this.param = param;
        this.configListener = new ConfigListener(args, param.getClassLoader());

    }

    public static EdgeApplication run(Class<?> param, String... args) {
        return new EdgeApplication(param, args);
    }

    //生成对应服务
//    public ServerContext official(SERVER server) {
//        switch (server.getCode()) {
//
//        }
//
//
//    }


}
