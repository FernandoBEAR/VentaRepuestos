package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.FechaMovimiento;
import com.venta.repuestos.entidades.Movimiento;
import com.venta.repuestos.entidades.Repuesto;
import com.venta.repuestos.enums.TipoMovimiento;
import com.venta.repuestos.exceptions.RepuestoNotFoundException;
import com.venta.repuestos.mappers.RepuestoMapper;
import com.venta.repuestos.repositorios.MovimientoRepository;
import com.venta.repuestos.repositorios.RepuestoRepository;
import com.venta.repuestos.servicios.RepuestoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RepuestoServiceImpl implements RepuestoService {
    @Autowired
    private RepuestoRepository repuestoRepository;
    @Autowired
    private RepuestoMapper repuestoMapper;
    @Autowired
    private MovimientoRepository movimientoRepository;

    public Repuesto crearRepuesto(Repuesto repuesto) {

        Repuesto repuestoExistente = repuestoRepository.findByNombre(repuesto.getNombre());

        if (repuestoExistente != null) {
            throw new IllegalArgumentException("El repuesto con nombre " + repuesto.getNombre() + " ya existe.");
        }

        Repuesto repuestoGuardado = repuestoRepository.save(repuesto);

        if (repuestoGuardado.getStock() != null && repuestoGuardado.getStock() > 0) {
            crearMovimiento(repuestoGuardado, TipoMovimiento.ENTRADA, repuestoGuardado.getStock());
        }

        return repuestoGuardado;
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
    public Repuesto actualizarRepuesto(Long id, RepuestoDTO repuesto) throws RepuestoNotFoundException{
        Repuesto repuestoExistente = obtenerRepuestoPorId(id);
        repuestoExistente.setNombre(repuesto.getNombre());
        repuestoExistente.setMarca(repuesto.getMarca());
        repuestoExistente.setDescripcion(repuesto.getDescripcion());
        repuestoExistente.setPrecio(repuesto.getPrecio());
        repuestoExistente.setStock(repuesto.getStock());
        repuestoRepository.save(repuestoExistente);
        return repuestoExistente;
    }

    @Override
    public Repuesto aumentarStock(Long id, int cantidad) throws RepuestoNotFoundException{
        Repuesto repuesto = obtenerRepuestoPorId(id);

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor que cero.");
        }
        // actualizar el stock.
        repuesto.setStock(repuesto.getStock() + cantidad);
        //Crear movimiento
        crearMovimiento(repuesto, TipoMovimiento.ENTRADA, cantidad);

        //Guardamos el repuesto actualizado en la base de datos.
        return repuestoRepository.save(repuesto);
    }


    @Override
    public Repuesto reducirStock(Long id, int cantidad) throws RepuestoNotFoundException{
        //Buscamos el repuesto.
        Repuesto repuesto = obtenerRepuestoPorId(id);

        //Validamos que la cantidad sea positiva.
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a reducir debe ser mayor que cero.");
        }

        //Validamos que haya stock suficiente.
        if (repuesto.getStock() < cantidad) {
            throw new IllegalStateException("No hay stock suficiente para el repuesto: " + repuesto.getNombre());
        }

        //Actualizamos el stock.
        repuesto.setStock(repuesto.getStock() - cantidad);

        //Creamos movimiento
        crearMovimiento(repuesto, TipoMovimiento.VENTA, cantidad);

        //Guardamos el repuesto actualizado.
        return repuestoRepository.save(repuesto);
    }

    @Override
    public Repuesto aumentarStockPorDevolucion(Long id, int cantidad) throws RepuestoNotFoundException{
        Repuesto repuesto = obtenerRepuestoPorId(id);

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor que cero.");
        }
        // actualizar el stock.
        repuesto.setStock(repuesto.getStock() + cantidad);
        //Crear movimiento
        crearMovimiento(repuesto, TipoMovimiento.DEVOLUCION, cantidad);

        //Guardamos el repuesto actualizado en la base de datos.
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

    private void crearMovimiento(Repuesto repuesto, TipoMovimiento tipo, Integer cantidad) {
        LocalDate fecha = LocalDate.now();
        FechaMovimiento fechaMovimiento = new FechaMovimiento(fecha.getDayOfMonth(), fecha.getMonthValue(), fecha.getYear());

        Movimiento movimiento = new Movimiento();
        movimiento.setFechaMovimiento(fechaMovimiento);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidadDeMovimientos(cantidad);
        movimiento.setRepuesto(repuesto);

        movimientoRepository.save(movimiento);
    }
}
