package com.mycompany.productosapp.db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author user
 */
public class DBManager {

    private static DBManager manager = null;
    private final String driverClass = "org.postgresql.Driver";
    private final String connectionString;
    private final String user;
    private final String password;

    private DBManager() throws Exception {
        Class.forName(driverClass);
        connectionString = System.getProperty("db_connection_string");
        user = System.getProperty("db_username");
        password = System.getProperty("db_user_password");
    }

    public static DBManager getInstance() throws Exception {
        if (manager == null) {
            manager = new DBManager();
        }
        return manager;
    }

    public Connection abrirConexion() throws Exception {
        return DriverManager.getConnection(connectionString,
                user,
                password);
    }

    public void cerrarConexion(Connection conexion) throws Exception {
        if (conexion != null && !conexion.isClosed()) {
            conexion.close();
        }
    }
}
