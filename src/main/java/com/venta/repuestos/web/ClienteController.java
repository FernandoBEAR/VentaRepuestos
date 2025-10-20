package com.venta.repuestos.web;

import com.venta.repuestos.entidades.Cliente;
import com.venta.repuestos.exceptions.ClienteNotFoundException;
import com.venta.repuestos.servicios.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> list() {
        return clienteService.findAll();
    }

    @GetMapping("/{id}")
    public Cliente getById(@PathVariable Long id) {
        return clienteService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Cliente> create(@RequestBody Cliente cliente) {
        Cliente saved = clienteService.save(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            Cliente updated = clienteService.update(id, cliente);
            return ResponseEntity.ok(updated);
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            clienteService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (ClienteNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Manejador global simple para la excepción (opcional)
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ClienteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
