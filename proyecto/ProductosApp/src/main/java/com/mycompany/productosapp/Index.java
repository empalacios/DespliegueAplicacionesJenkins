package com.mycompany.productosapp;

import com.mycompany.productosapp.db.ProductosManager;
import com.mycompany.productosapp.objetos.Producto;
import java.io.Serializable;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author user
 */
@ManagedBean(name = "index")
@ViewScoped
public class Index implements Serializable {

    private ProductosManager productosManager;
    private List<Producto> productos;
    private Producto productoSeleccionado;

    public Index() throws Exception {
        productosManager = ProductosManager.getInstance();
        productos = productosManager.listarProductos();
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProductoSeleccionado(Producto productoSeleccionado) {
        this.productoSeleccionado = productoSeleccionado;
    }

}
