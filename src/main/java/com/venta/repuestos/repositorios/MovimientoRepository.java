package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByRepuestoId(Long repuestoId);
}
