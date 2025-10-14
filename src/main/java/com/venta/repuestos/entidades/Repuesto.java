package com.venta.repuestos.entidades;

import com.venta.repuestos.enums.Disponibilidad;
import com.venta.repuestos.enums.Marca;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Repuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Marca marca;
    private String descripcion;
    private Double precio;
    private Integer stock;
    private Disponibilidad disponibilidad;
}
