package com.github.kurban.miitroombooking.util;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class ConnectionManager {
    private static final String URL_KEY = "db.url";
    private static final String USERNAME_KEY = "db.username";
    private static final String PASSWORD_KEY = "db.password";
    private static final String SIZE_KEY = "db.pool.size";
    private static BlockingQueue<Connection> pool;

    static {
        extracted();
        initConnectionPool();
    }

    private static void initConnectionPool() {
        String s = PropertiesUtil.get(SIZE_KEY);
        pool = new ArrayBlockingQueue<Connection>(Integer.parseInt(s));
        for (int i = 0; i < Integer.parseInt(s); i++) {
            Connection open = open();
            Connection close = (Connection) Proxy.newProxyInstance(ConnectionManager.class.getClassLoader(), new Class[]{Connection.class},
                    ((proxy, method, args) -> method.getName().equals("close") ? proxy : method.invoke(open, args)));
            pool.add(close);
        }
    }

    private static void extracted() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection get(){
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private ConnectionManager() {}

    private static Connection open(){
        try{
            return DriverManager.getConnection(
                    PropertiesUtil.get(URL_KEY),
                    PropertiesUtil.get(USERNAME_KEY),
                    PropertiesUtil.get(PASSWORD_KEY)
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
