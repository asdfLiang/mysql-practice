package org.example.isolation;

import org.example.support.MysqlDB;
import org.example.support.ResultUtil;
import org.example.support.UUIDUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 可重复读隔离级别 - 幻读问题
 *
 * @since 2024/12/12 16:15
 * @author by liangzj9624
 */
public class RepeatableReadPhantomRead {
    public static void main(String[] args) throws InterruptedException {
        // 初始化数据，避免重复执行干扰
        initData();
        new Thread(RepeatableReadPhantomRead::transaction1).start();
        TimeUnit.SECONDS.sleep(1);
        new Thread(RepeatableReadPhantomRead::transaction2).start();
    }

    private static void initData() {
        try (Connection connection = MysqlDB.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM test_task WHERE process_id = 2");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transaction1() {
        try (Connection connection = MysqlDB.getConnection()) {
            MysqlDB.startRepeatableReadTransaction(connection);
            Statement stat = connection.createStatement();
            System.out.println("事务1开启");
            // 第一次快照读，没有数据
            ResultSet rs1 = stat.executeQuery("SELECT * FROM test_task WHERE process_id = 2");
            List<Object> list1 = ResultUtil.getList(rs1, Object.class);
            System.out.println("事务1第一次快照读: " + list1.size());

            // 停顿5秒，等另一个事务执行完(另一个事务会插入一条数据)
            System.out.println("事务1阻塞");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("事务1继续执行");

            // 修改前快照读，没有数据
            ResultSet rs2 = stat.executeQuery("SELECT * FROM test_task WHERE process_id = 2");
            List<Object> list2 = ResultUtil.getList(rs2, Object.class);
            System.out.println("事务1修改前快照读: " + list2.size());

            // 修改数据
            System.out.println("事务1修改任务数据");
            stat.executeUpdate("UPDATE test_task SET task_status = 1 WHERE process_id = 2");

            // 修改后快照读，产生幻读，读到一条数据
            ResultSet rs3 = stat.executeQuery("SELECT * FROM test_task WHERE process_id = 2");
            List<Object> list3 = ResultUtil.getList(rs3, Object.class);
            System.out.println("事务1修改后快照读: " + list3.size());

            connection.commit();
            System.out.println("事务1执行完成");
        } catch (SQLException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transaction2() {
        try (Connection connection = MysqlDB.getConnection()) {
            MysqlDB.startRepeatableReadTransaction(connection);
            Statement statement = connection.createStatement();
            System.out.println("事务2开启");

            // 插入数据
            statement.execute(
                    "INSERT INTO "
                            + "test_task(task_id, task_name, task_status, process_id, task_deleted) "
                            + "VALUES ('"
                            + UUIDUtil.getUUID()
                            + "', '事务2插入任务', 0, 2, false)");
            System.out.println("事务2插入一条数据");

            connection.commit();
            System.out.println("事务2执行完成");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
