package com.felip.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felip.model.Laboratorio;
import com.felip.model.Producto;
import com.felip.repository.LaboratorioRepository;
import com.felip.repository.ProductoRepository;

@Service
public class ProductoDAO {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    public boolean registrar(String nombre, String descripcion, int idLaboratorio) {
        Optional<Laboratorio> laboratorio = laboratorioRepository.findById(idLaboratorio);

        if (laboratorio.isEmpty()) {
            System.out.println("Laboratorio no encontrado.");
            return false;
        }

        productoRepository.save(new Producto(nombre, descripcion, laboratorio.get()));
        return true;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public boolean actualizar(int id, String nombre, String descripcion, int idLaboratorio) {
        Optional<Producto> producto = productoRepository.findById(id);
        Optional<Laboratorio> laboratorio = laboratorioRepository.findById(idLaboratorio);

        if (producto.isEmpty() || laboratorio.isEmpty()) {
            System.out.println("Producto o laboratorio no encontrado.");
            return false;
        }

        Producto p = producto.get();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setLaboratorio(laboratorio.get());

        productoRepository.save(p);
        return true;
    }

    public boolean eliminar(int id) {
        if (!productoRepository.existsById(id)) {
            System.out.println("Producto no encontrado.");
            return false;
        }

        productoRepository.deleteById(id);
        return true;
    }
}