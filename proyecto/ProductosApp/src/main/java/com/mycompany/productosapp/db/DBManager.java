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
        /*
         * La información sobre la configuración se obtiene a partir de variables
         * entorno. En este caso, ya que la aplicación se ejecutará en el servidor
         * Payara, se definen dichas variables en como propiedades del servidor de
         * aplicaciones.
         */
        connectionString = System.getProperty("db_connection_string");
        user = System.getProperty("db_username");
        password = System.getProperty("db_user_password");
        if (connectionString == null || connectionString.trim().isEmpty()
                || user == null || user.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new Exception("Ha ocurrido un error, no se han creado las variables de entorno de la aplicación.\n"
                    + "  Para crear las variables de entorno debe:\n"
                    + "    * Ingresar a consola de administración del servidor de aplicaciones\n"
                    + "    * Ir a la opción server (Admin Server) del menú\n"
                    + "    * Ir a la pestaña Properties\n"
                    + "    * Add Property (nombre de la propiedad y valor de la propiedad)\n"
                    + "      Las propiedades a configurar son:\n"
                    + "        * db_connection_string: url de conexión a la base de datos\n"
                    + "        * db_username: usuario de base de datos con la que se conecta la aplicación\n"
                    + "        * db_user_password: contraseña del usuario de base de datos\n"
                    + "  Recargar la aplicación para que obtenga la configuración"
                    + "    * Ir a la opción Applications del menú\n"
                    + "    * Dar click en el enlace Reload correspondiente a nuestra aplicación");
        }
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
