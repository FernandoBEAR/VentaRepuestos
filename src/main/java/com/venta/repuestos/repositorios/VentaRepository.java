package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Carga ventas con sus detalles y pago en una sola consulta (JOIN FETCH)
     * para evitar LazyInitializationException al poblar el grafo RDF.
     */
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.repuesto " +
           "LEFT JOIN FETCH v.pago " +
           "LEFT JOIN FETCH v.cliente")
    List<Venta> findAllWithDetallesAndPago();
}
