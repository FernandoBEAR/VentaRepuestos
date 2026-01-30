package com.venta.repuestos.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.venta.repuestos.enums.EstadoPago;
import com.venta.repuestos.enums.MetodoPago;
import com.venta.repuestos.enums.TipoComprobante;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private double monto;
    private LocalDate Fecha;
    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago;
    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;
    @Enumerated(EnumType.STRING)
    private TipoComprobante tipoComprobante;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta")
    @JsonBackReference
    private Venta venta;
}
