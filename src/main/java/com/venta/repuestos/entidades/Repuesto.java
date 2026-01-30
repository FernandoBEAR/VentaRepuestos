package com.venta.repuestos.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.venta.repuestos.enums.Marca;
import com.venta.repuestos.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Repuesto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Enumerated(EnumType.STRING)
    private Marca marca;

    private String descripcion;

    private Double precio;

    private Integer stock;

    @OneToMany(mappedBy = "repuesto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //@JsonManagedReference
    private List<Movimiento> movimientos = new ArrayList<>();

}
