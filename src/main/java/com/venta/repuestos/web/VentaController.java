package com.venta.repuestos.web;


import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.entidades.Venta;
import com.venta.repuestos.exceptions.RecursoNoEncontradoException;
import com.venta.repuestos.repositorios.VentaRepository;
import com.venta.repuestos.servicios.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {
    @Autowired
    private VentaService ventaService;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaService.listarVentas();
    }

    @GetMapping("/{id}")
    public Venta obtenerVentaPorId(@PathVariable(name = "id") Long id) {
        return ventaService.obtenerVentaPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la venta con ID " + id));
    }

    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@RequestBody Venta venta) {
        Venta nuevaVenta = ventaService.registrarVenta(venta);
        return ResponseEntity.ok(nuevaVenta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Venta> actualizarVenta(@PathVariable Long id, @RequestBody Venta ventaActualizada) {
        Venta venta = ventaService.obtenerVentaPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la venta con ID " + id));

        venta.setCliente(ventaActualizada.getCliente());
        venta.setDetalles(ventaActualizada.getDetalles());
        venta.setTotal(ventaActualizada.getTotal());

        Venta ventaGuardada = ventaService.registrarVenta(venta);
        return ResponseEntity.ok(ventaGuardada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long id) {
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleVenta>> obtenerDetallesPorVenta(@PathVariable Long id) {
        Venta venta = ventaService.obtenerVentaPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la venta con ID " + id));
        return ResponseEntity.ok(venta.getDetalles());
    }

}
