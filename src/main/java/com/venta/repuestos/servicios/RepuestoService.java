package com.venta.repuestos.servicios;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.exceptions.RepuestoNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RepuestoService {
    //CREAR
    Repuesto crearRepuesto(Repuesto repuesto);
    //OBTENER
    Repuesto obtenerRepuestoPorId(Long id) throws RepuestoNotFoundException;
    RepuestoDTO obtenerRepuestoDTOPorId(Long id) throws RepuestoNotFoundException;
    Repuesto obtenerRepuestoPorNombre(String nombre) throws RepuestoNotFoundException;
    List<Repuesto> obtenerTodosLosRepuestos();
    List<RepuestoDTO> obtenerTodosLosRepuestosDTO();
    //ACTUALIZAR
    Repuesto actualizarRepuesto(Long id, Repuesto repuesto) throws RepuestoNotFoundException;
    Repuesto aumentarStock(Long id, int cantidad)throws RepuestoNotFoundException;
    Repuesto reducirStock(Long id, int cantidad)throws RepuestoNotFoundException;
    //ELIMINAR
    void eliminarRepuesto(Long id) throws RepuestoNotFoundException;
}
