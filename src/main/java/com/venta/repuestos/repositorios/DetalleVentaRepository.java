package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta,Long> {
    List<DetalleVenta> findByVentaId(Long ventaId);
}
