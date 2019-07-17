package top.soliloquize;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author wb
 * @date 2019/7/17
 */
public class SoliloquizeThreadPoolImpl implements SoliloquizeThreadPool {

    private static String driver = null;
    private static String url = null;
    private static String user = null;
    private static String password = null;
    /**
     * 连接池中管道参数
     **/
    private static int initCount = 5;
    private static int stepSize = 10;
    private static int poolMaxSize = 55;
    private static int expandTime = 0;
    /**
     * 连接池
     * 线程安全集合,用来放(可复用)数据库连接管道
     */
    private static volatile CopyOnWriteArrayList<PooledConnection> pooledConnections = new CopyOnWriteArrayList<>();

    public SoliloquizeThreadPoolImpl() {
        this.init();
    }

    @Override
    public PooledConnection getConnection() {
        //要拿连接,先判断连接池中是否有连接
        if (pooledConnections.size() == 0) {
            System.out.println("连接池没有连接！");
            //如果没有就手动再建一把连接池
            this.createConnections(initCount);
        }
        PooledConnection connection = getRealConnection();
        //如果还是没有拿到,说明全部线程都处于busy状态,得扩容
        while (connection == null) {
            this.createConnections(stepSize);
            connection = getRealConnection();
            try {//拿到连接等待一会,防止连接又被别的线程抢夺
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * 同步方法,真正的获取连接(连接包装内包括:connection和标志位busy)
     *
     * @return PooledConnection
     */
    private  PooledConnection getRealConnection() {
        for (PooledConnection conn : pooledConnections) {
            //判断该连接是否已被占用,false为可用(空闲),true为占用(繁忙)
            if (!conn.isBusy()) {
                Connection connection = conn.getConnection();
                try {
                    //判断该连接是否在设定时间连接通数据库(连接通为true)
                    if (!connection.isValid(2000)) {
                        connection = DriverManager.getConnection(url, user, password);
                    }
                    conn.setConnection(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                conn.setBusy(true);
                return conn;
            }
        }
        //如果连接池中的连接都被占用,则返回null,由调用函数处理(即扩容)
        return null;
    }

    @Override
    public synchronized void createConnections(int count) {
        expandTime++;
        System.out.println("第" + expandTime + "次扩容,扩容量为:" + count);
        if ((pooledConnections.size() + count) <= poolMaxSize) {
            for (int i = 0; i < count; i++) {
                try {
                    //获取连接放入线程安全的连接池中
                    Connection conn = DriverManager.getConnection(url, user, password);
                    PooledConnection pooledConnection = new PooledConnection(false, conn, String.valueOf(i));
                    pooledConnections.add(pooledConnection);
                    System.out.println("初始化" + (i + 1) + "个连接");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("当前连接池连接数量:" + pooledConnections.size());
        System.out.println("最大连接池数量为:" + poolMaxSize);
    }

    private void init() {
        InputStream inStream = this.getClass().getClassLoader().getResourceAsStream("jdbc.properties");
        Properties properties = new Properties();
        try {
            properties.load(inStream);
        } catch (IOException e) {
            //若这里抛出异常则下面不运行
            e.printStackTrace();
        }
        driver = properties.getProperty("jdbc_driver");
        url = properties.getProperty("jdbc_url");
        user = properties.getProperty("jdbc_username");
        password = properties.getProperty("jdbc_password");
        String inCountStr = properties.getProperty("initCount");
        String stepSizeStr = properties.getProperty("stepSize");
        String poolMaxSizeStr = properties.getProperty("poolMaxSize");
        if (inCountStr != null && Integer.valueOf(inCountStr) > 0) {
            initCount = Integer.valueOf(properties.getProperty("initCount"));
        } else if (stepSizeStr != null && Integer.valueOf(stepSizeStr) > 0) {
            stepSize = Integer.valueOf(properties.getProperty("stepSize"));
        } else if (poolMaxSizeStr != null && Integer.valueOf(poolMaxSizeStr) > 0) {
            poolMaxSize = Integer.valueOf(properties.getProperty("poolMaxSize"));
        }
        //准备创建DriverManager
        try {
            Driver dbDriver = (Driver) Class.forName(driver).newInstance();
            DriverManager.registerDriver(dbDriver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取连接,用create方法获取
        this.createConnections(initCount);
    }
}
