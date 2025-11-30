package com.venta.repuestos.dtos;

import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public record AuthCreateRoleRequest(
        @Size(max = 3, message = "The user cannot haver more than 3 roles") List<String> roleListName) {
}