package top.soliloquize;


/**
 * @author wb
 * @date 2019/7/17
 * 连接池架构接口
 */
public interface SoliloquizeThreadPool {
    /**
     * 对外提供可复用连接
     *
     * @return PooledConnection
     */
    PooledConnection getConnection();

    /**
     * 对内创建连接
     *
     * @param count 连接数两
     */
    void createConnections(int count);
}
