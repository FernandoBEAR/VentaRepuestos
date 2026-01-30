package com.venta.repuestos.web;

import com.venta.repuestos.entidades.Pago;
import com.venta.repuestos.exceptions.ClienteNotFoundException;
import com.venta.repuestos.servicios.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/pagos")
public class PagoController {

    @Autowired
    private PagoService service;

    @GetMapping
    public List<Pago> list() {
        return service.lista();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        Optional<Pago> op = service.porId(id);
        if (op.isPresent()) {
            return ResponseEntity.ok(op.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Pago pago) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crearPago(pago));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Pago pago) {
        Optional<Pago> op = service.porId(id);
        if (op.isPresent()) {
            Pago pagoBD = op.get();
            pagoBD.setEstadoPago(pago.getEstadoPago());
            pagoBD.setFecha(pago.getFecha());
            pagoBD.setMetodoPago(pago.getMetodoPago());
            pagoBD.setTipoComprobante(pago.getTipoComprobante());
            pagoBD.setMonto(pago.getMonto());
            return ResponseEntity.ok(service.crearPago(pagoBD));
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/{pagado}")
    public ResponseEntity<?> cambiarEstado(@PathVariable Long id, @PathVariable boolean pagado) {
        Optional<Pago> op = service.porId(id);
        if (op.isPresent()) {
            return ResponseEntity.ok(service.actualizarEstado(pagado,id));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.eliminarPago(id);
            return ResponseEntity.noContent().build();
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}