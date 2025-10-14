package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepository extends JpaRepository<Venta,Long> {
}
