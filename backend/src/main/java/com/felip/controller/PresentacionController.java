package com.felip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felip.dao.PresentacionDAO;
import com.felip.model.Presentacion;

@RestController
@RequestMapping("/presentaciones")
public class PresentacionController {

    @Autowired
    private PresentacionDAO presentacionDAO;

    @GetMapping
    public List<Presentacion> listarPresentaciones() {
        return presentacionDAO.obtenerTodas();
    }
}
