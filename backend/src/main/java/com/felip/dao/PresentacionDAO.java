package com.felip.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felip.model.Presentacion;
import com.felip.model.Producto;
import com.felip.repository.PresentacionRepository;
import com.felip.repository.ProductoRepository;

@Service
public class PresentacionDAO {

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public boolean registrar(int cantidad, String unidadMedida, BigDecimal precio, int idProducto) {
        Optional<Producto> producto = productoRepository.findById(idProducto);

        if (producto.isEmpty()) {
            System.out.println("Producto no encontrado.");
            return false;
        }

        presentacionRepository.save(new Presentacion(cantidad, unidadMedida, precio, producto.get()));
        return true;
    }

    public List<Presentacion> obtenerTodas() {
        return presentacionRepository.findAll();
    }

    public boolean actualizar(int id, int cantidad, String unidadMedida, BigDecimal precio, int idProducto) {
        Optional<Presentacion> presentacion = presentacionRepository.findById(id);
        Optional<Producto> producto = productoRepository.findById(idProducto);

        if (presentacion.isEmpty() || producto.isEmpty()) {
            System.out.println("Presentación o producto no encontrado.");
            return false;
        }

        Presentacion p = presentacion.get();
        p.setCantidad(cantidad);
        p.setUnidadMedida(unidadMedida);
        p.setPrecio(precio);
        p.setProducto(producto.get());

        presentacionRepository.save(p);
        return true;
    }

    public boolean eliminar(int id) {
        if (!presentacionRepository.existsById(id)) {
            System.out.println("Presentación no encontrada.");
            return false;
        }

        presentacionRepository.deleteById(id);
        return true;
    }
}
