package com.venta.repuestos.dtos;

import lombok.Data;

import java.util.List;

@Data
public class VentaDTO {
    private Long id;
    private String clienteNombre;
    private Double total;
    private List<DetalleVentaDTO> detalles;
}
