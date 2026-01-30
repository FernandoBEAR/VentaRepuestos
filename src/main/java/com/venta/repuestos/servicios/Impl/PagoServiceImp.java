package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.entidades.Pago;
import com.venta.repuestos.enums.EstadoPago;
import com.venta.repuestos.repositorios.PagoRepository;
import com.venta.repuestos.servicios.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PagoServiceImp implements PagoService {

    @Autowired
    private PagoRepository repository;

    @Override
    public Pago crearPago(Pago pago) {
        pago.setFecha(LocalDate.now());
        pago.setEstadoPago(EstadoPago.POR_PAGAR);
        return repository.save(pago);
    }

    @Override
    public Optional<Pago> porId(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Pago> lista() {
        return (List<Pago>) repository.findAll();
    }

    @Override
    public void eliminarPago(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Pago> actualizarEstado(boolean pagado,Long id) {
        Optional<Pago> op = repository.findById(id);
        if (op.isPresent()) {
            Pago pagoBD = op.get();
            pagoBD.setEstadoPago(pagado ? EstadoPago.PAGADO: EstadoPago.POR_PAGAR);
            return Optional.of(repository.save(pagoBD));
        }
        return op;
    }
}