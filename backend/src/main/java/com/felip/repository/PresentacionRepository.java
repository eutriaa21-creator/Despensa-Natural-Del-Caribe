package com.felip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.felip.model.Presentacion;

@Repository
public interface PresentacionRepository extends JpaRepository<Presentacion, Integer> {
    java.util.List<Presentacion> findByProducto_IdProducto(Integer productoId);
}
