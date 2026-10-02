package com.felip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felip.dao.ProductoDAO;
import com.felip.model.Producto;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoDAO productoDAO;

    @GetMapping
    public List<Producto> listarProductos() {
        return productoDAO.obtenerTodos();
    }
}
