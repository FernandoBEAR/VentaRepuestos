package com.venta.repuestos.web;

import java.util.*;

/**
 * Parser de lenguaje natural para consultas semánticas.
 *
 * Detecta intenciones y extrae parámetros de un mensaje en texto libre.
 * No requiere librerías externas: usa comparación de palabras clave.
 *
 * Ejemplos de mensajes soportados:
 *   "Listame todos los repuestos de la marca BOSCH"
 *   "Dame los productos de STANLEY pagados con efectivo"
 *   "Muestra los repuestos de TRUPPER vendidos con tarjeta de crédito"
 *   "Quiero ver los repuestos vendidos con yape"
 */
public class NaturalLanguageQueryParser {

    // ── Marcas conocidas ──────────────────────────────────────────────────────
    private static final List<String> MARCAS = List.of("BOSCH", "STANLEY", "TRUPPER");

    // ── Métodos de pago (orden: más específico primero) ───────────────────────
    private static final Map<String, String> METODOS_PAGO = new LinkedHashMap<>();
    static {
        METODOS_PAGO.put("tarjeta de credito",  "TARJETA_CREDITO");
        METODOS_PAGO.put("tarjeta de crédito",  "TARJETA_CREDITO");
        METODOS_PAGO.put("tarjeta credito",     "TARJETA_CREDITO");
        METODOS_PAGO.put("tarjeta de debito",   "TARJETA_DEBITO");
        METODOS_PAGO.put("tarjeta de débito",   "TARJETA_DEBITO");
        METODOS_PAGO.put("tarjeta debito",      "TARJETA_DEBITO");
        METODOS_PAGO.put("transferencia",       "TRANSFERENCIA");
        METODOS_PAGO.put("efectivo",            "EFECTIVO");
        METODOS_PAGO.put("yape",                "YAPE");
        METODOS_PAGO.put("plin",                "PLIN");
        METODOS_PAGO.put("tarjeta",             "TARJETA_CREDITO"); // fallback genérico
    }

    // ── Entidades (orden: más específico primero) ─────────────────────────────
    private static final Map<String, String> ENTIDADES = new LinkedHashMap<>();
    static {
        ENTIDADES.put("repuesto",    "REPUESTO");
        ENTIDADES.put("repuestos",   "REPUESTO");
        ENTIDADES.put("producto",    "REPUESTO");
        ENTIDADES.put("productos",   "REPUESTO");
        ENTIDADES.put("cliente",     "CLIENTE");
        ENTIDADES.put("clientes",    "CLIENTE");
        ENTIDADES.put("venta",       "VENTA");
        ENTIDADES.put("ventas",      "VENTA");
        ENTIDADES.put("pago",        "PAGO");
        ENTIDADES.put("pagos",       "PAGO");
        ENTIDADES.put("movimiento",  "MOVIMIENTO");
        ENTIDADES.put("movimientos", "MOVIMIENTO");
    }

    /**
     * Parsea el texto libre y devuelve un mapa con los parámetros detectados:
     *   "entidad"    → REPUESTO | CLIENTE | VENTA | PAGO | MOVIMIENTO
     *   "marca"      → BOSCH | STANLEY | TRUPPER  (o null si no se detecta)
     *   "metodoPago" → EFECTIVO | TARJETA_CREDITO | ... (o null si no se detecta)
     */
    public static Map<String, String> parsear(String texto) {
        String lower = texto.toLowerCase();
        String upper = texto.toUpperCase();

        Map<String, String> resultado = new LinkedHashMap<>();

        // 1. Entidad (default: REPUESTO)
        resultado.put("entidad", "REPUESTO");
        for (Map.Entry<String, String> e : ENTIDADES.entrySet()) {
            if (lower.contains(e.getKey())) {
                resultado.put("entidad", e.getValue());
                break;
            }
        }

        // 2. Marca
        for (String marca : MARCAS) {
            if (upper.contains(marca)) {
                resultado.put("marca", marca);
                break;
            }
        }

        // 3. Método de pago (orden importa: "tarjeta de crédito" antes que "tarjeta")
        for (Map.Entry<String, String> e : METODOS_PAGO.entrySet()) {
            if (lower.contains(e.getKey())) {
                resultado.put("metodoPago", e.getValue());
                break;
            }
        }

        return resultado;
    }
}

