package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.repositorios.DetalleVentaRepository;
import com.venta.repuestos.servicios.DetalleVentaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DetalleVentaServicioImpl implements DetalleVentaServicio {


    @Autowired
    private DetalleVentaRepository detalleVentaRepository;


    @Override
    public DetalleVenta guardarDetalle(DetalleVenta detalle) {
        return detalleVentaRepository.save(detalle);
    }

    @Override
    public List<DetalleVenta> listarDetalles() {
        return detalleVentaRepository.findAll();
    }

    @Override
    public Optional<DetalleVenta> obtenerDetallePorId(Long id) {
        return detalleVentaRepository.findById(id);
    }

    @Override
    public List<DetalleVenta> obtenerDetallesPorVenta(Long ventaId) {
        return detalleVentaRepository.findByVentaId(ventaId);
    }

    @Override
    public void eliminarDetalle(Long id) {
        detalleVentaRepository.deleteById(id);
    }
}
