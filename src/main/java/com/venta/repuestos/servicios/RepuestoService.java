package com.venta.repuestos.servicios;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RepuestoService {
    //CREAR
    Repuesto crearRepuesto(Repuesto repuesto);
    //OBTENER
    Repuesto obtenerRepuestoPorId(Long id);
    Repuesto obtenerRepuestoPorNombre(String nombre);
    List<Repuesto> obtenerTodosLosRepuestos();
    List<RepuestoDTO> obtenerTodosLosRepuestosDTO();
    //ACTUALIZAR
    Repuesto actualizarRepuesto(Long id, Repuesto repuesto);
    Repuesto aumentarStock(Long id, int cantidad);
    Repuesto reducirStock(Long id, int cantidad);
    //ELIMINAR
    void eliminarRepuesto(Long id);
}
