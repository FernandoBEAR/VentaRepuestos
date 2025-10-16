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
        if (venta.getDetalles() != null) {
            venta.getDetalles().forEach(detalle -> detalle.setVenta(venta));
            venta.getDetalles().forEach(detalleVenta ->
                    repuestoService.reducirStock(detalleVenta.getRepuesto().getId(),detalleVenta.getCantidad()));

            venta.getDetalles().forEach(
                    producto -> producto.setRepuesto(repuestoService.obtenerRepuestoPorId(producto.getRepuesto().getId())));
        }

        double total = venta.getDetalles()
                .stream()
                .mapToDouble(DetalleVenta::getSubtotal)
                .sum();
        venta.setTotal(total);



        venta.setCliente(clienteService.findById(venta.getCliente().getId()));
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
