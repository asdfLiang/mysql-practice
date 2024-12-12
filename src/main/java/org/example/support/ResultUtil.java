package org.example.support;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 2024/12/12 17:31
 * @author by liangzj9624
 */
public class ResultUtil {

    public static <T> List<T> getList(ResultSet rs, Class<T> clazz) throws SQLException {
        List<T> list = new ArrayList<>();
        while (rs.next()) {
            list.add(getObject(rs, clazz));
        }
        return list;
    }

    public static Map<String, Object> getMap(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Object> map = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            map.put(metaData.getColumnName(i), rs.getObject(i));
        }

        return map;
    }

    public static <T> T getObject(ResultSet rs, Class<T> clazz) throws SQLException {
        return JsonUtil.parse(JsonUtil.from(getMap(rs)), clazz);
    }
}
