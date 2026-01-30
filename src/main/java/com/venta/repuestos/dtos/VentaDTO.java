package com.venta.repuestos.dtos;

import com.venta.repuestos.entidades.Pago;
import lombok.Data;

import java.util.List;

@Data
public class VentaDTO {
    private Long id;
    private String clienteNombre;
    private Double total;
    private List<DetalleVentaDTO> detalles;
    private Pago pago;
}
