package com.felip.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.felip.model.Lote;
import com.felip.model.Presentacion;
import com.felip.repository.LoteRepository;
import com.felip.repository.PresentacionRepository;

@Service
public class LoteDAO {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    public boolean registrar(LocalDate fechaIngreso, LocalDate fechaVencimiento, int cantidad, int idPresentacion) {
        Optional<Presentacion> presentacion = presentacionRepository.findById(idPresentacion);

        if (presentacion.isEmpty()) {
            System.out.println("Presentación no encontrada.");
            return false;
        }

        loteRepository.save(new Lote(fechaIngreso, fechaVencimiento, cantidad, presentacion.get()));
        return true;
    }

    public List<Lote> obtenerTodos() {
        return loteRepository.findAll();
    }

    public boolean actualizar(int id, LocalDate fechaIngreso, LocalDate fechaVencimiento, int cantidad, int idPresentacion) {
        Optional<Lote> lote = loteRepository.findById(id);
        Optional<Presentacion> presentacion = presentacionRepository.findById(idPresentacion);

        if (lote.isEmpty() || presentacion.isEmpty()) {
            System.out.println("Lote o presentación no encontrados.");
            return false;
        }

        Lote l = lote.get();
        l.setFechaIngreso(fechaIngreso);
        l.setFechaVencimiento(fechaVencimiento);
        l.setCantidad(cantidad);
        l.setPresentacion(presentacion.get());

        loteRepository.save(l);
        return true;
    }

    public boolean eliminar(int id) {
        if (!loteRepository.existsById(id)) {
            System.out.println("Lote no encontrado.");
            return false;
        }

        loteRepository.deleteById(id);
        return true;
    }

    public List<Lote> obtenerPorVencer(int dias) {
        LocalDate fechaLimite = LocalDate.now().plusDays(dias);
        return loteRepository.findByFechaVencimientoLessThanEqual(fechaLimite);
    }
}