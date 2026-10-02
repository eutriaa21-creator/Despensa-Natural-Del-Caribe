package com.felip.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felip.model.Lote;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {
    List<Lote> findByFechaVencimientoLessThanEqual(LocalDate fechaLimite);
    List<Lote> findByPresentacion_Producto_IdProducto(Integer productoId);
}
