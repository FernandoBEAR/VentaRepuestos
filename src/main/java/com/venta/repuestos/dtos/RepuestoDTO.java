package com.venta.repuestos.dtos;


import com.venta.repuestos.enums.Marca;
import lombok.Data;

@Data
public class RepuestoDTO {
    private Long id;
    private String nombre;
    private Marca marca;
    private String descripcion;
    private Double precio;
    private Integer stock;
}
