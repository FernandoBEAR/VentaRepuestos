package com.venta.repuestos.web;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.servicios.RepuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/repuestos")
public class RepuestoController {
    @Autowired
    private RepuestoService repuestoService;

    @PostMapping("/guardar")
    public Repuesto agregarRepuesto(@RequestBody Repuesto repuesto) {
       return repuestoService.crearRepuesto(repuesto);
    }

    @GetMapping("/listar")
    public List<Repuesto> listarRepuestos() {
        return repuestoService.obtenerTodosLosRepuestos();
    }
    @GetMapping("/listarDTO")
    public List<RepuestoDTO> listarRepuestosDTO() {
        return repuestoService.obtenerTodosLosRepuestosDTO();
    }

    @GetMapping("/buscar/{nombre}")
    public Repuesto obtenerRepuestoPorNombre(@PathVariable String nombre) {
        return repuestoService.obtenerRepuestoPorNombre(nombre);
    }

    @GetMapping("/buscar/id/{id}")
    public RepuestoDTO obtenerRepuestoPorId(@PathVariable Long id) {
        return repuestoService.obtenerRepuestoDTOPorId(id);
    }

    @PutMapping("/actualizar/{id}")
    public Repuesto actualizarRepuesto(@PathVariable Long id, @RequestBody Repuesto repuesto) {
        return repuestoService.actualizarRepuesto(id, repuesto);
    }

    @PutMapping("/{id}/aumentar-stock/{cantidad}")
    public Repuesto aumentarStock(@PathVariable Long id, @PathVariable int cantidad) { // Recibimos la cantidad como un parámetro en la URL

        Repuesto repuestoActualizado = repuestoService.aumentarStock(id, cantidad);
        return repuestoActualizado;
    }

    @PutMapping("/{id}/reducir-stock/{cantidad}")
    public Repuesto reducirStock(@PathVariable Long id, @PathVariable int cantidad) {

        Repuesto repuestoActualizado = repuestoService.reducirStock(id, cantidad);
        return repuestoActualizado;
    }


    @DeleteMapping("/eliminar/{id}")
    public void eliminarRepuesto(@PathVariable Long id) {
        repuestoService.eliminarRepuesto(id);
    }
}
