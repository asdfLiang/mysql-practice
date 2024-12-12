package org.example.isolation;

import org.example.support.MysqlDB;
import org.example.support.UUIDUtil;

import java.sql.*;
import java.util.concurrent.TimeUnit;

/**
 * 可重复读导隔离级别 - 导致重复插入问题
 *
 * <p>每个流程对应一个任务
 *
 * @since 2024/12/12 09:40
 * @author by liangzj9624
 */
public class RepeatableReadDuplicateInsert {
    public static void main(String[] args) throws InterruptedException {
        // 数据初始化，防止多次执行干扰结果
        initData();
        // 事务1先开启，开启后等事务2执行完，再继续执行插入
        new Thread(RepeatableReadDuplicateInsert::transaction1).start();
        TimeUnit.SECONDS.sleep(1);
        // 事务2后开启，先执行完
        new Thread(RepeatableReadDuplicateInsert::transaction2).start();
    }

    private static void initData() {
        try (Connection connection = MysqlDB.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("DELETE FROM test_task WHERE process_id = 1");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void transaction1() {
        try (Connection connection = MysqlDB.getConnection()) {
            MysqlDB.startRepeatableReadTransaction(connection);
            Statement statement = connection.createStatement();
            System.out.println("事务1开启");
            // 第一次读（如果没有第一次读，此时不会生成快照，阻塞后就能查到事务2插入的数据）
            ResultSet resultSet1 =
                    statement.executeQuery("SELECT * FROM test_task WHERE process_id = 1");
            System.out.println("事务1第一次读: " + resultSet1.next());

            // 停顿5秒，等另一个事务执行完
            System.out.println("事务1阻塞");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("事务1继续执行");

            // 重复读，检查process是否有任务（此时另一个事务已经插入数据了，但因为可重复读，所以查不到）
            boolean hasData =
                    statement.executeQuery("SELECT * FROM test_task WHERE process_id = 1").next();
            System.out.println("事务1重复读: " + hasData);
            if (hasData) {
                System.out.println("事务1查到了插入的数据，中断执行");
                return;
            }
            System.out.println("事务1未查到插入的数据");

            // 插入数据任务(这里就是重复插入了)
            statement.execute(
                    "INSERT INTO "
                            + "test_task(task_id, task_name, task_status, process_id, task_deleted) "
                            + "VALUES ('"
                            + UUIDUtil.getUUID()
                            + "', '事务1插入任务', 0, 1, false)");

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

            // 检查process是否有任务
            ResultSet resultSet =
                    statement.executeQuery("SELECT * FROM test_task WHERE process_id = 1");
            if (resultSet.next()) {
                System.out.println("事务2查到了插入的数据，中断执行");
                return;
            }

            // 插入数据
            statement.execute(
                    "INSERT INTO "
                            + "test_task(task_id, task_name, task_status, process_id, task_deleted) "
                            + "VALUES ('"
                            + UUIDUtil.getUUID()
                            + "', '事务2插入任务', 0, 1, false)");

            connection.commit();
            System.out.println("事务2执行完成");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
