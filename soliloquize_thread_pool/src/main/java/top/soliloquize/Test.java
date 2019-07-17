package top.soliloquize;

import java.sql.ResultSet;

/**
 * @author wb
 * @date 2019/7/17
 */
public class Test {
    public static void main(String[] args) {
        SoliloquizeThreadPool pool = PoolManager.INSTANCE.get();
        for (int i = 0; i < 2000; i++) {
            new Thread(() -> {
                pool.getConnection();
                ResultSet resultSet = pool.getConnection().queryBySql("select * from user");
                try {
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        System.out.println(name);
                    }
                }catch (Exception e) {}
            }).start();
        }
    }
}
