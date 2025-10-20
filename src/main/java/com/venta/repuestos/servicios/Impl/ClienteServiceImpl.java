package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.entidades.Cliente;
import com.venta.repuestos.exceptions.ClienteNotFoundException;
import com.venta.repuestos.repositorios.ClienteRepository;
import com.venta.repuestos.servicios.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente findById(Long id) throws  ClienteNotFoundException {
        return clienteRepository.findById(id).orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado"));
    }

    @Override
    public Cliente save(Cliente cliente) {
        // KISS: permitir que JPA genere el id
        cliente.setId(null);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente update(Long id, Cliente cliente) {
        return clienteRepository.findById(id).map(existing -> {
            existing.setNombre(cliente.getNombre());
            existing.setEmail(cliente.getEmail());
            return clienteRepository.save(existing);
        }).orElseThrow(() -> new ClienteNotFoundException("Cliente no encontrado con id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }
}

