package com.felip.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felip.model.Laboratorio;
import com.felip.repository.LaboratorioRepository;

@Service
public class LaboratorioDAO {

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    public boolean registrar(String nombre, String ciudad) {
        if (nombre == null || nombre.isBlank() || ciudad == null || ciudad.isBlank()) {
            System.out.println("Nombre y ciudad son obligatorios.");
            return false;
        }

        laboratorioRepository.save(new Laboratorio(nombre, ciudad));
        return true;
    }

    public List<Laboratorio> obtenerTodos() {
        return laboratorioRepository.findAll();
    }

    public boolean actualizar(int id, String nombre, String ciudad) {
        Optional<Laboratorio> lab = laboratorioRepository.findById(id);

        if (lab.isEmpty()) {
            System.out.println("Laboratorio no encontrado.");
            return false;
        }

        Laboratorio laboratorio = lab.get();
        laboratorio.setNombre(nombre);
        laboratorio.setCiudad(ciudad);
        laboratorioRepository.save(laboratorio);
        return true;
    }

    public boolean eliminar(int id) {
        if (!laboratorioRepository.existsById(id)) {
            System.out.println("Laboratorio no encontrado.");
            return false;
        }

        laboratorioRepository.deleteById(id);
        return true;
    }
}
