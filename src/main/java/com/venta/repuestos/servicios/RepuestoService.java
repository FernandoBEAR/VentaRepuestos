package com.venta.repuestos.servicios;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RepuestoService {

    Repuesto crearRepuesto(Repuesto repuesto);
    Repuesto obtenerRepuestoPorId(Long id);
    Repuesto obtenerRepuestoPorNombre(String nombre);
    List<Repuesto> obtenerTodosLosRepuestos();
    List<RepuestoDTO> obtenerTodosLosRepuestosDTO();
    Repuesto actualizarRepuesto(Long id, Repuesto repuesto);
    void eliminarRepuesto(Long id);
}
