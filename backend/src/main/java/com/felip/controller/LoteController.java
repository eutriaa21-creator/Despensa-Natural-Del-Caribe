package com.felip.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felip.dao.LoteDAO;
import com.felip.model.Lote;

@RestController
@RequestMapping("/lotes")
public class LoteController {

    @Autowired
    private LoteDAO loteDAO;

    @GetMapping
    public List<Lote> listarLotes() {
        return loteDAO.obtenerTodos();
    }

    @GetMapping("/por-vencer")
    public List<Lote> listarLotesPorVencer() {
        return loteDAO.obtenerPorVencer(90);
    }
}
