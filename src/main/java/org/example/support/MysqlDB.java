package org.example.support;

import java.sql.*;

/**
 * mysql
 *
 * @since 2024/12/12 09:42
 * @author by liangzj9624
 */
public class MysqlDB {
    private static final String DRIVER_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName(DRIVER_NAME);
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** 开启可重复读隔离级别事务 */
    public static void startRepeatableReadTransaction(Connection connection) throws SQLException {
        // 注意：必须在开启事务前设置，否则不生效
        if (connection.getTransactionIsolation() != Connection.TRANSACTION_REPEATABLE_READ) {
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
        }

        // 开启事务
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
    }

    /** 开启读已提交隔离级别事务 */
    public static void startTransactionReadCommitted(Connection connection) throws SQLException {
        // 注意：必须在开启事务前设置，否则不生效
        if (connection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }

        // 开启事务
        if (connection.getAutoCommit()) {
            connection.setAutoCommit(false);
        }
    }
}
