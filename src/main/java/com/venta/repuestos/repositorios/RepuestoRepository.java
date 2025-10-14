package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {
    Repuesto findByNombre(String nombre);
}
