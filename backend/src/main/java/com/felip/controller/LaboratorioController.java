package com.felip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felip.dao.LaboratorioDAO;
import com.felip.model.Laboratorio;

@RestController
@RequestMapping("/laboratorios")
public class LaboratorioController {

    @Autowired
    private LaboratorioDAO laboratorioDAO;

    @GetMapping
    public List<Laboratorio> listarLaboratorios() {
        return laboratorioDAO.obtenerTodos();
    }
}