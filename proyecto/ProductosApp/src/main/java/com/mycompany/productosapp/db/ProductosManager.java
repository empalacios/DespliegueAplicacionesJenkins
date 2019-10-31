package com.mycompany.productosapp.db;

import com.mycompany.productosapp.objetos.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author user
 */
public class ProductosManager {

    private static final String SQL_LISTAR_TODOS = "SELECT id, nombre, descripcion"
            + "  FROM producto"
            + "  ORDER BY id";
    private static ProductosManager manager = null;
    private DBManager dbManager;

    private ProductosManager() throws Exception {
        dbManager = DBManager.getInstance();
    }

    public static ProductosManager getInstance() throws Exception {
        if (manager == null) {
            manager = new ProductosManager();
        }
        return manager;
    }

    public List<Producto> listarProductos() throws Exception {
        List<Producto> productos = new ArrayList<>();
        Connection conexion = dbManager.abrirConexion();
        PreparedStatement statement = conexion.prepareStatement(SQL_LISTAR_TODOS);
        ResultSet result = statement.executeQuery();

        while (result.next()) {
            Producto producto = new Producto();

            producto.setId(result.getInt("id"));
            producto.setNombre(result.getString("nombre"));
            producto.setDescripcion(result.getString("descripcion"));
            productos.add(producto);
        }
        dbManager.cerrarConexion(conexion);
        return productos;
    }
}
