package com.venta.repuestos.web;

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

    @GetMapping("/buscar/{nombre}")
    public Repuesto obtenerRepuestoPorNombre(@PathVariable String nombre) {
        return repuestoService.obtenerRepuestoPorNombre(nombre);
    }

    @PutMapping("/actualizar/{id}")
    public Repuesto actualizarRepuesto(@PathVariable Long id, @RequestBody Repuesto repuesto) {
        return repuestoService.actualizarRepuesto(id, repuesto);
    }

    @DeleteMapping("/eliminar/{id}")
    public void eliminarRepuesto(@PathVariable Long id) {
        repuestoService.eliminarRepuesto(id);
    }
}
