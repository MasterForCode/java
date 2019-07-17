package top.soliloquize;

/**
 * @author wb
 * @date 2019/7/17
 * 提供线程池实例
 */
public enum PoolManager {
    /**
     * 连接池实例
     */
    INSTANCE;

    public SoliloquizeThreadPool get() {
        return new SoliloquizeThreadPoolImpl();
    }

}
