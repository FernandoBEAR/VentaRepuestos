package com.venta.repuestos.entidades;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;
    @ManyToOne
    @JoinColumn(name = "repuesto_id", nullable = false)
    private Repuesto repuesto;
    private int cantidad;
    private Double precioUnitario;
    private Double subtotal;
}
