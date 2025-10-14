package com.venta.repuestos.mappers;

import com.venta.repuestos.dtos.DetalleVentaDTO;
import com.venta.repuestos.dtos.VentaDTO;
import com.venta.repuestos.entidades.DetalleVenta;
import com.venta.repuestos.entidades.Venta;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaMapper {
    public VentaDTO mapearDeVenta(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setId(venta.getId());
        dto.setClienteNombre(venta.getCliente().getNombre());
        dto.setTotal(venta.getTotal());

        List<DetalleVentaDTO> detallesDTO = venta.getDetalles().stream()
                .map(this::mapearDeDetalle)
                .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

        return dto;
    }

    private DetalleVentaDTO mapearDeDetalle(DetalleVenta detalle) {
        DetalleVentaDTO dto = new DetalleVentaDTO();
        dto.setId(detalle.getId());
        dto.setNombreRepuesto(detalle.getRepuesto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }
}
