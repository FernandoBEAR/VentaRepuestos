package com.venta.repuestos.mappers;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.entidades.Repuesto;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class RepuestoMapper {

    public RepuestoDTO mapearDeRepuestoADTO(Repuesto repuesto) {
        RepuestoDTO repuestoDTO = new RepuestoDTO();
        BeanUtils.copyProperties(repuesto, repuestoDTO);
        return repuestoDTO;
    }
    public Repuesto mapearDeRepuestoADTO(RepuestoDTO repuestoDTO) {
        Repuesto repuesto = new Repuesto();
        BeanUtils.copyProperties(repuestoDTO, repuesto);
        return repuesto;
    }
}
