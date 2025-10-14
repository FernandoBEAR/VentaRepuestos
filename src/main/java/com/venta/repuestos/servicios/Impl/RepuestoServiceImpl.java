package com.venta.repuestos.servicios.Impl;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
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
    public Repuesto obtenerRepuestoPorId(Long id) {
        return repuestoRepository.findById(id).orElse(null);
    }

    @Override
    public Repuesto obtenerRepuestoPorNombre(String nombre) {
        return repuestoRepository.findByNombre(nombre);
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
    public Repuesto actualizarRepuesto(Long id, Repuesto repuesto) {
        return repuestoRepository.save(repuesto);
    }

    @Override
    public void eliminarRepuesto(Long id) {
        repuestoRepository.deleteById(id);
    }
}
