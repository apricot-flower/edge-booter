package on.edge.jdbc;

import on.edge.except.ConfigException;
import on.edge.except.JDBCException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * JDBC处理器
 */
@SuppressWarnings("all")
public class JDBCManager {

    private static final String ORMS_NAME = "orms";

    private static final String ORM_NAME = "orm";

    private static final String TABLE_NAME = "table_name";

    private String name;

    private Map<String, JDBCStaff> staff;

    public JDBCManager() {
        this.staff = new HashMap<>();
    }

    public JDBCManager(String name, Map<String, JDBCStaff> staff) {
        this.name = name;
        this.staff = staff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, JDBCStaff> getStaff() {
        return staff;
    }

    public void setStaff(Map<String, JDBCStaff> staff) {
        this.staff = staff;
    }

    public JDBCManager build(File file, EdgeJDBCListener edgeJDBCListener) throws Exception {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        InputStream input = new FileInputStream(file);
        Document doc = domBuilder.parse(input);
        Element root = doc.getDocumentElement();
        if (!root.getTagName().equals(ORMS_NAME)) {
            throw new ConfigException(file.getName() + " not in the correct format!");
        }
        this.name = root.getAttribute(TABLE_NAME);
        if (this.name == null || this.name.trim().equals("")) {
            throw new ConfigException(file.getName() + " this item is not configured:" + TABLE_NAME);
        }
        NodeList childNodes = root.getChildNodes();
        for (int index = 0; index < childNodes.getLength(); index ++) {
            Node node = childNodes.item(index);
            String nodeName = node.getNodeName();
            if (nodeName.trim().equals(ORM_NAME)) {
                JDBCStaff jdbcStaff = new JDBCStaff().build(node, edgeJDBCListener);
                this.staff.put(jdbcStaff.getName(), jdbcStaff);
            }
        }
        input.close();
        return this;
    }

    public JDBCOperateBuilder operate(String staffName) {
        if (!this.staff.containsKey(staffName)) {
            throw new JDBCException(this.name + " does not has " + staffName);
        }
        JDBCStaff jdbcStaff = this.staff.get(staffName);
        return new JDBCOperateBuilder(jdbcStaff);
    }
}
