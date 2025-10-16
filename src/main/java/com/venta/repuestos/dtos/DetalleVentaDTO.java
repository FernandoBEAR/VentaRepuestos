package com.venta.repuestos.dtos;

import lombok.Data;

@Data
public class DetalleVentaDTO {
    private Long id;
    private String nombreRepuesto;
    private int cantidad;
    private Double subtotal;
}
