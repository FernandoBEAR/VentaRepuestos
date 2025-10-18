package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.entidades.Venta;
import com.venta.repuestos.repositorios.DetalleVentaRepository;
import com.venta.repuestos.repositorios.VentaRepository;
import com.venta.repuestos.servicios.ClienteService;
import com.venta.repuestos.servicios.RepuestoService;
import com.venta.repuestos.servicios.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VentaServicioImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private RepuestoService repuestoService;

    @Autowired
    private ClienteService clienteService;


    @Override
    public Venta registrarVenta(Venta venta) {
        // Buscar cliente por Id
        venta.setCliente(clienteService.findById(venta.getCliente().getId()));
        if (venta.getDetalles() != null) {
            double total = 0.0;
            for (DetalleVenta detalle : venta.getDetalles()) {
                detalle.setVenta(venta);
                // Buscar repuesto en BD
                Repuesto repuesto = repuestoService.obtenerRepuestoPorId(detalle.getRepuesto().getId());
                if (repuesto.getStock() < detalle.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + repuesto.getNombre());
                }
                // Calcular totales
                detalle.setPrecioUnitario(repuesto.getPrecio());
                detalle.setSubtotal(repuesto.getPrecio() * detalle.getCantidad());
                total += detalle.getSubtotal();

                repuestoService.reducirStock(repuesto.getId(), detalle.getCantidad());
                detalle.setRepuesto(repuesto);
            }
            venta.setTotal(total);
        }
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Override
    public void eliminarVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    @Override
    public Optional<Venta> obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id);
    }
}
