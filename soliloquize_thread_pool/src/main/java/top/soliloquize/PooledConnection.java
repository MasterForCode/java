package top.soliloquize;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author wb
 * @date 2019/7/17
 */
@Data
@AllArgsConstructor
public class PooledConnection {
    /**
     * 表示繁忙标志     复用的标志
     */
    private boolean busy;
    /**
     * 真正的sql 连接connection(java.sql.Connection)
     */
    private Connection connection;
    /**
     * 只是用来测试当前connectionName,便于观察
     */
    private String connName;

    /**
     * 将该连接置为不可用,而不是真正关掉连接
     */
    public void close() {
        this.busy = false;
    }

    /**
     * 对外提供一个简单的测试方法，也就是获得了连接之后，就可以使用statement进行执行Sql语句
     *
     * @param sql sql语句
     * @return ResultSet
     */
    public ResultSet queryBySql(String sql) {
        Statement sttm;
        ResultSet rs = null;
        try {
            sttm = connection.createStatement();
            rs = sttm.executeQuery(sql);
            System.out.println("当前连接编号是:" + connName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
}
