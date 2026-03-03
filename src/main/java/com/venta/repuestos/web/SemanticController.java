package com.venta.repuestos.web;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.repositorios.SparqlRepository;
import com.venta.repuestos.servicios.OntologyLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la capa de Web Semántica.
 *
 * Todos los datos son cargados dinámicamente desde la BD por
 * OntologyLoaderService.
 *
 * Entidades disponibles: Repuesto, Cliente, Venta, DetalleVenta, Pago,
 * Movimiento.
 */
@RestController
@RequestMapping("/api/semantico")
public class SemanticController {
    @Autowired
    private SparqlRepository sparqlRepository;

    @Autowired
    private com.venta.repuestos.servicios.RepuestoService repuestoService;

    @Autowired
    private com.venta.repuestos.servicios.ClienteService clienteService;

    @Autowired
    private OntologyLoaderService ontologyLoaderService;

    /**
     * POST /api/semantico/sincronizar
     * Sincroniza todos los repuestos y clientes desde MySQL hacia el Grafo RDF.
     */
    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizarMasivamente() {
        int repCount = 0;
        int clienteCount = 0;

        try {
            List<RepuestoDTO> repuestos = repuestoService.obtenerTodosLosRepuestosDTO();
            for (RepuestoDTO r : repuestos) {
                // Usamos actualizar para forzar un UPSERT (borra y re-escribe)
                sparqlRepository.actualizarRepuesto(r);
                repCount++;
            }

            List<com.venta.repuestos.entidades.Cliente> clientes = clienteService.findAll();
            for (com.venta.repuestos.entidades.Cliente c : clientes) {
                sparqlRepository.actualizarCliente(c);
                clienteCount++;
            }

            return ResponseEntity.ok("Sincronización exitosa. Registros migrados: " + repCount + " repuestos, "
                    + clienteCount + " clientes.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error durante la sincronización masiva: " + e.getMessage());
        }
    }

    // =========================================================================
    // REPUESTOS
    // =========================================================================

    /** GET /api/semantico/repuestos — Todos los repuestos del grafo RDF. */
    @GetMapping("/repuestos")
    public ResponseEntity<List<RepuestoDTO>> listarRepuestos() {
        return ResponseEntity.ok(sparqlRepository.buscarTodosRepuestos());
    }

    /**
     * GET /api/semantico/repuestos/marca/{marca} — Filtrar por marca (BOSCH,
     * STANLEY, TRUPPER).
     */
    @GetMapping("/repuestos/marca/{marca}")
    public ResponseEntity<List<RepuestoDTO>> buscarPorMarca(@PathVariable String marca) {
        List<RepuestoDTO> resultado = sparqlRepository.buscarRepuestosPorMarca(marca.toUpperCase());
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    /**
     * GET /api/semantico/repuestos/buscar?q=taladro — Búsqueda semántica por texto.
     */
    @GetMapping("/repuestos/buscar")
    public ResponseEntity<List<RepuestoDTO>> buscarRepuestosPorTexto(@RequestParam String q) {
        return ResponseEntity.ok(sparqlRepository.buscarRepuestosPorTexto(q));
    }

    // =========================================================================
    // CLIENTES
    // =========================================================================

    /** GET /api/semantico/clientes — Todos los clientes del grafo RDF. */
    @GetMapping("/clientes")
    public ResponseEntity<List<Map<String, String>>> listarClientes() {
        return ResponseEntity.ok(sparqlRepository.buscarClientes());
    }

    /**
     * GET /api/semantico/clientes/buscar?nombre=juan — Buscar clientes por nombre.
     */
    @GetMapping("/clientes/buscar")
    public ResponseEntity<List<Map<String, String>>> buscarClientesPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(sparqlRepository.buscarClientesPorNombre(nombre));
    }

    // =========================================================================
    // VENTAS
    // =========================================================================

    /** GET /api/semantico/ventas — Todas las ventas con cliente y total. */
    @GetMapping("/ventas")
    public ResponseEntity<List<Map<String, String>>> listarVentas() {
        return ResponseEntity.ok(sparqlRepository.buscarVentas());
    }

    /**
     * GET /api/semantico/ventas/cliente?nombre=juan — Ventas filtradas por nombre
     * de cliente.
     */
    @GetMapping("/ventas/cliente")
    public ResponseEntity<List<Map<String, String>>> buscarVentasPorCliente(@RequestParam String nombre) {
        List<Map<String, String>> resultado = sparqlRepository.buscarVentasPorCliente(nombre);
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    // =========================================================================
    // DETALLE VENTA
    // =========================================================================

    /** GET /api/semantico/detalles — Todos los detalles de venta. */
    @GetMapping("/detalles")
    public ResponseEntity<List<Map<String, String>>> listarDetalles() {
        return ResponseEntity.ok(sparqlRepository.buscarDetallesVenta());
    }

    /**
     * GET /api/semantico/detalles/venta/{ventaId} — Detalles de una venta
     * específica.
     */
    @GetMapping("/detalles/venta/{ventaId}")
    public ResponseEntity<List<Map<String, String>>> detallesPorVenta(@PathVariable String ventaId) {
        List<Map<String, String>> resultado = sparqlRepository.buscarDetallesPorVenta(ventaId);
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    // =========================================================================
    // PAGOS
    // =========================================================================

    /** GET /api/semantico/pagos — Todos los pagos con estado y método. */
    @GetMapping("/pagos")
    public ResponseEntity<List<Map<String, String>>> listarPagos() {
        return ResponseEntity.ok(sparqlRepository.buscarPagos());
    }

    /**
     * GET /api/semantico/pagos/estado/{estado} — Filtrar por estado (PAGADO,
     * PENDIENTE, ANULADO).
     */
    @GetMapping("/pagos/estado/{estado}")
    public ResponseEntity<List<Map<String, String>>> buscarPagosPorEstado(@PathVariable String estado) {
        List<Map<String, String>> resultado = sparqlRepository.buscarPagosPorEstado(estado.toUpperCase());
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    // =========================================================================
    // MOVIMIENTOS
    // =========================================================================

    /** GET /api/semantico/movimientos — Todos los movimientos de inventario. */
    @GetMapping("/movimientos")
    public ResponseEntity<List<Map<String, String>>> listarMovimientos() {
        return ResponseEntity.ok(sparqlRepository.buscarMovimientos());
    }

    /**
     * GET /api/semantico/movimientos/tipo/{tipo} — Filtrar por tipo (VENTA,
     * ENTRADA, DEVOLUCION).
     */
    @GetMapping("/movimientos/tipo/{tipo}")
    public ResponseEntity<List<Map<String, String>>> buscarMovimientosPorTipo(@PathVariable String tipo) {
        List<Map<String, String>> resultado = sparqlRepository.buscarMovimientosPorTipo(tipo.toUpperCase());
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    /**
     * GET /api/semantico/movimientos/repuesto?nombre=taladro — Filtrar por nombre
     * de repuesto.
     */
    @GetMapping("/movimientos/repuesto")
    public ResponseEntity<List<Map<String, String>>> buscarMovimientosPorRepuesto(@RequestParam String nombre) {
        List<Map<String, String>> resultado = sparqlRepository.buscarMovimientosPorRepuesto(nombre);
        if (resultado.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    // =========================================================================
    // CONSULTA POR LENGUAJE NATURAL
    // =========================================================================

    /**
     * POST /api/semantico/consulta/natural
     *
     * Interpreta un mensaje en texto libre y ejecuta la consulta SPARQL
     * correspondiente en el grafo RDF.
     *
     * Body: { "mensaje": "Listame todos los repuestos de la marca BOSCH" }
     *
     * Ejemplos de mensajes:
     *   "Dame los productos de STANLEY pagados con efectivo"
     *   "Muestra los repuestos de TRUPPER con tarjeta de crédito"
     *   "Quiero ver las ventas"
     *   "Muestra los clientes"
     */
    @PostMapping("/consulta/natural")
    public ResponseEntity<Map<String, Object>> consultaNatural(@RequestBody Map<String, String> body) {
        String mensaje = body.get("mensaje");
        if (mensaje == null || mensaje.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error",   "El campo 'mensaje' es obligatorio.",
                    "ejemplo", "{ \"mensaje\": \"Listame los repuestos de BOSCH\" }"));
        }

        // 1. Parsear intención y parámetros del mensaje
        Map<String, String> parametros = NaturalLanguageQueryParser.parsear(mensaje);
        String entidad    = parametros.get("entidad");
        String marca      = parametros.get("marca");
        String metodoPago = parametros.get("metodoPago");

        // 2. Ejecutar consulta SPARQL según entidad detectada
        List<Map<String, String>> datos = switch (entidad) {
            case "REPUESTO"    -> sparqlRepository.buscarRepuestosVendidosPorMarcaYMetodoPago(marca, metodoPago);
            case "CLIENTE"     -> sparqlRepository.buscarClientes();
            case "VENTA"       -> sparqlRepository.buscarVentas();
            case "PAGO"        -> sparqlRepository.buscarPagos();
            case "MOVIMIENTO"  -> sparqlRepository.buscarMovimientos();
            default            -> sparqlRepository.buscarRepuestosVendidosPorMarcaYMetodoPago(marca, metodoPago);
        };

        // 3. Construir respuesta con contexto para que el cliente vea la interpretación
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("mensajeRecibido",      mensaje);
        respuesta.put("parametrosDetectados", parametros);
        respuesta.put("totalResultados",      datos.size());

        if (datos.isEmpty()) {
            respuesta.put("info",       "No se encontraron resultados para los filtros detectados.");
            respuesta.put("resultados", List.of());
        } else {
            respuesta.put("resultados", datos);
        }

        return ResponseEntity.ok(respuesta);
    }

    // =========================================================================
    // SPARQL LIBRE Y RECARGA
    // =========================================================================

    /**
     * POST /api/semantico/sparql
     * Ejecuta una consulta SPARQL libre contra el grafo.
     * Body: { "query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10" }
     */
    @PostMapping("/sparql")
    public ResponseEntity<List<Map<String, String>>> ejecutarSparql(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (query == null || query.isBlank())
            return ResponseEntity.badRequest().build();
        try {
            return ResponseEntity.ok(sparqlRepository.ejecutarConsultaLibre(query));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/semantico/recargar
     * Fuerza la resincronización del grafo RDF con los datos actuales de la BD.
     */
    @PostMapping("/recargar")
    public ResponseEntity<String> recargarGrafo() {
        try {
            ontologyLoaderService.recargar();
            return ResponseEntity.ok("Grafo RDF resincronizado con la base de datos exitosamente.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al recargar el grafo RDF: " + e.getMessage());
        }
    }
}
