package com.venta.repuestos.servicios;

import com.venta.repuestos.entidades.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaService {
    Venta registrarVenta(Venta venta);
    List<Venta> listarVentas();
    void eliminarVenta(Long id);
    Optional<Venta> obtenerVentaPorId(Long id);
}
