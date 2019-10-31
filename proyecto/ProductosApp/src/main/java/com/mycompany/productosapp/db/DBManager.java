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
    private final String connectionString = "jdbc:postgresql://127.0.0.1:5432/app";
    private final String user = "app";
    private final String password = "app";

    private DBManager() throws Exception {
        Class.forName(driverClass);
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
