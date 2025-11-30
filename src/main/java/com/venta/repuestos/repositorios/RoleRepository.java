package com.venta.repuestos.repositorios;

import com.venta.repuestos.entidades.securityentities.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity,Long> {

    //Trae los roles que existan en la base de datos
    List<RoleEntity> findRoleEntitiesByRoleEnumIn(List<String> roleNames);
}
