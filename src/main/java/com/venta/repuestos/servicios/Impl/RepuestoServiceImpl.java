package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.exceptions.RepuestoNotFoundException;
import com.venta.repuestos.mappers.RepuestoMapper;
import com.venta.repuestos.repositorios.RepuestoRepository;
import com.venta.repuestos.servicios.RepuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepuestoServiceImpl implements RepuestoService {
    @Autowired
    private RepuestoRepository repuestoRepository;
    @Autowired
    private RepuestoMapper repuestoMapper;

    @Override
    public Repuesto crearRepuesto(Repuesto repuesto) {
        return repuestoRepository.save(repuesto);
    }


    @Override
    public Repuesto obtenerRepuestoPorId(Long id) throws RepuestoNotFoundException {
        return repuestoRepository.findById(id).orElseThrow(()-> new RepuestoNotFoundException("Repuesto no encontrado"));
    }

    @Override
    public RepuestoDTO obtenerRepuestoDTOPorId(Long id) throws RepuestoNotFoundException {
        Repuesto repuesto = obtenerRepuestoPorId(id);
        return repuestoMapper.mapearDeRepuestoADTO(repuesto);
    }

    @Override
    public Repuesto obtenerRepuestoPorNombre(String nombre) throws RepuestoNotFoundException {
        Repuesto repuesto = repuestoRepository.findByNombre(nombre);
        if (repuesto==null){
            throw new RepuestoNotFoundException("Repuesto no encontrado");
        }
        return repuesto;
    }

    @Override
    public List<Repuesto> obtenerTodosLosRepuestos() {
        return repuestoRepository.findAll();
    }

    @Override
    public List<RepuestoDTO> obtenerTodosLosRepuestosDTO() {
        List<Repuesto> repuestos = repuestoRepository.findAll();
        List<RepuestoDTO> repuestoDTOS =repuestos.stream().map(repuesto -> repuestoMapper.mapearDeRepuestoADTO(repuesto))
                .collect(Collectors.toList());
        return repuestoDTOS;
    }

    @Override
    public Repuesto actualizarRepuesto(Long id, Repuesto repuesto) throws RepuestoNotFoundException{
        Repuesto repuestoExistente = obtenerRepuestoPorId(id);
        return repuestoExistente;
    }

    @Override
    public Repuesto aumentarStock(Long id, int cantidad) throws RepuestoNotFoundException{
        // 1. Buscamos el repuesto por su ID. Si no existe, el método de abajo lanzará una excepción.
        Repuesto repuesto = obtenerRepuestoPorId(id);

        // 2. Validamos que la cantidad sea positiva.
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor que cero.");
        }

        // 3. Actualizamos el stock.
        repuesto.setStock(repuesto.getStock() + cantidad);

        // 4. Guardamos el repuesto actualizado en la base de datos.
        return repuestoRepository.save(repuesto);
    }


    @Override
    public Repuesto reducirStock(Long id, int cantidad) throws RepuestoNotFoundException{
        // 1. Buscamos el repuesto.
        Repuesto repuesto = obtenerRepuestoPorId(id);

        // 2. Validamos que la cantidad sea positiva.
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reducir debe ser mayor que cero.");
        }

        // 3. ✨ Lógica de negocio CRÍTICA: Validamos que haya stock suficiente.
        if (repuesto.getStock() < cantidad) {
            throw new IllegalStateException("No hay stock suficiente para el repuesto: " + repuesto.getNombre());
        }

        // 4. Actualizamos el stock.
        repuesto.setStock(repuesto.getStock() - cantidad);

        // 5. Guardamos el repuesto actualizado.
        return repuestoRepository.save(repuesto);
    }

    @Override
    public void eliminarRepuesto(Long id) throws RepuestoNotFoundException {
        Repuesto repuesto = obtenerRepuestoPorId(id);
        if (repuesto==null){
            throw new RepuestoNotFoundException("Repuesto no encontrado");
        }
        repuestoRepository.deleteById(id);
    }
}
