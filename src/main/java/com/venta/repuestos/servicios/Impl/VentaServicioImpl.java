package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.entidades.Venta;
import com.venta.repuestos.repositorios.DetalleVentaRepository;
import com.venta.repuestos.repositorios.VentaRepository;
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


    @Override
    public Venta registrarVenta(Venta venta) {
        if (venta.getDetalles() != null) {
            venta.getDetalles().forEach(detalle -> detalle.setVenta(venta));
        }

        double total = venta.getDetalles()
                .stream()
                .mapToDouble(DetalleVenta::getSubtotal)
                .sum();
        venta.setTotal(total);

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
