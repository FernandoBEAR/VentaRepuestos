package com.venta.repuestos.exceptions;

public class RepuestoNotFoundException extends RuntimeException {
    public RepuestoNotFoundException(String message) {
        super(message);
    }
}
