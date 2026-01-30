package com.venta.repuestos.servicios;


import com.venta.repuestos.entidades.Pago;

import java.util.List;
import java.util.Optional;

public interface PagoService {
    //CREAR
    Pago crearPago (Pago pago);
    //OBTENER
    Optional<Pago> porId(Long id);
    List<Pago> lista();
    //ELIMINAR
    void eliminarPago(Long id);
    //ACTUALIZAR
    Optional<Pago> actualizarEstado(boolean pagado, Long id);
}