package com.venta.repuestos.servicios;

import com.venta.repuestos.entidades.DetalleVenta;

import java.util.List;
import java.util.Optional;

public interface DetalleVentaServicio {
    DetalleVenta guardarDetalle(DetalleVenta detalle);
    List<DetalleVenta> listarDetalles();
    Optional<DetalleVenta> obtenerDetallePorId(Long id);
    List<DetalleVenta> obtenerDetallesPorVenta(Long ventaId);
    void eliminarDetalle(Long id);
}
