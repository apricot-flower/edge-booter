package on.edge.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import on.edge.except.JDBCException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("all")
public class JDBCOperateBuilder {

    private static final String INSERT = "insert";
    private static final String DELETE = "delete";
    private static final String UPDATE = "update";
    private static final String SELECT = "select";


    private Map<String, Object> params;

    private JDBCStaff staff;

    public JDBCOperateBuilder(JDBCStaff staff) {
        this.params = new HashMap<>();
        this.staff = staff;
    }

    public JDBCOperateBuilder addParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public JDBCOperateBuilder append(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    public Object load() {
        String sqlFrame = this.staff.getSqlFrame();
        try {
            if (sqlFrame == null || sqlFrame.trim().equals("")) {
                throw new JDBCException(this.staff.getName() + " does not has sql!");
            }
            String operate = this.staff.getOperate() == null ? null : this.staff.getOperate().trim().toLowerCase();
            if (operate == null || operate.equals("")) {
                //直接执行sql
                return this.staff.load(sqlFrame,operate);
            } else if (!operate.equals(INSERT) && !operate.equals(UPDATE) && !operate.equals(DELETE) && !operate.equals(SELECT)) {
                return this.staff.load(sqlFrame,operate);
            } else {
                sqlFrame = translateSql(sqlFrame);
            }
            Object value = this.staff.load(sqlFrame,operate);
            switch (operate) {
                case INSERT:
                case UPDATE:
                case DELETE:
                    return value;
                case SELECT:
                    return translateRrsult(value, this.staff.getResultType());
                default:
                    return value;
            }
        } catch (Exception e) {
            throw new JDBCException(sqlFrame + ". error:" + e.getMessage());
        }
    }

    private Object translateRrsult(Object value, String resultType) throws Exception {
        if (value == null) {
            return value;
        }
        ResultSet resultSet = (ResultSet) value;
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Object> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = resultSet.getObject(i);
                row.put(columnName, columnValue);
            }
            if (resultType != null && !resultType.trim().equals("")) {
                Class<?> clazz = Class.forName(resultType.trim());
                result.add(new ObjectMapper().convertValue(row, clazz));
            } else {
                result.add(row);
            }
        }
        return result;
    }


    private String translateSql(String sqlFrame) {
        ArrayList<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\$\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(sqlFrame);
        while (matcher.find()) {
            matches.add(matcher.group(1)); // group(1)获取括号内的内容
        }
        for (String param : matches) {
            String paramValue = "";
            if (this.params.containsKey(param)) {
                paramValue = String.valueOf(this.params.get(param));
            }
            sqlFrame = sqlFrame.replace(String.join("", "${", param, "}"), "'" + paramValue + "'");
        }
        return sqlFrame;
    }
}
