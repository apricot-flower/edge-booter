package on.edge.jdbc;

import org.w3c.dom.Node;

/**
 * 配置文件中单个orm
 */
@SuppressWarnings("all")
public class JDBCStaff {
    //sql名称
    private String name;
    private static final String NAME = "name";

    //操作类型
    private String operate;
    private static final String OPERATE = "operate";

    //sql语句
    private String sqlFrame;
    private static final String SQL_FRAME = "sql";

    //返回结果
    private String resultType;
    private static final String RESULT_TYPE = "resultType";


    private EdgeJDBCListener edgeJDBCListener;

    public JDBCStaff() {
    }


    public JDBCStaff(String name, String operate, String sqlFrame, String resultType) {
        this.name = name;
        this.operate = operate;
        this.sqlFrame = sqlFrame;
        this.resultType = resultType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public String getSqlFrame() {
        return sqlFrame;
    }

    public void setSqlFrame(String sqlFrame) {
        this.sqlFrame = sqlFrame;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public JDBCStaff build(Node node, EdgeJDBCListener edgeJDBCListener) {
        this.edgeJDBCListener = edgeJDBCListener;
        this.name = node.getAttributes().getNamedItem(NAME) == null ? "" : node.getAttributes().getNamedItem(NAME).getNodeValue();
        this.operate = node.getAttributes().getNamedItem(OPERATE) == null ? "" : node.getAttributes().getNamedItem(OPERATE).getNodeValue();
        this.sqlFrame = node.getAttributes().getNamedItem(SQL_FRAME) == null ? "" : node.getAttributes().getNamedItem(SQL_FRAME).getNodeValue();
        this.resultType = node.getAttributes().getNamedItem(RESULT_TYPE) == null ? "" : node.getAttributes().getNamedItem(RESULT_TYPE).getNodeValue();
        return this;
    }

    public Object load(String sqlFrame, String operate) throws Exception {
        return this.edgeJDBCListener.load(sqlFrame, operate);
    }
}
