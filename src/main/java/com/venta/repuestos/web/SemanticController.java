package com.venta.repuestos.web;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.repositorios.SparqlRepository;
import com.venta.repuestos.servicios.OntologyLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la capa de Web Semántica.
 *
 * Todos los datos son cargados dinámicamente desde la BD por OntologyLoaderService.
 *
 * Entidades disponibles: Repuesto, Cliente, Venta, DetalleVenta, Pago, Movimiento.
 */
@RestController
@RequestMapping("/api/semantico")
public class SemanticController {

    @Autowired
    private SparqlRepository sparqlRepository;

    @Autowired
    private OntologyLoaderService ontologyLoaderService;

    // =========================================================================
    // REPUESTOS
    // =========================================================================

    /** GET /api/semantico/repuestos — Todos los repuestos del grafo RDF. */
    @GetMapping("/repuestos")
    public ResponseEntity<List<RepuestoDTO>> listarRepuestos() {
        return ResponseEntity.ok(sparqlRepository.buscarTodosRepuestos());
    }

    /** GET /api/semantico/repuestos/marca/{marca} — Filtrar por marca (BOSCH, STANLEY, TRUPPER). */
    @GetMapping("/repuestos/marca/{marca}")
    public ResponseEntity<List<RepuestoDTO>> buscarPorMarca(@PathVariable String marca) {
        List<RepuestoDTO> resultado = sparqlRepository.buscarRepuestosPorMarca(marca.toUpperCase());
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    /** GET /api/semantico/repuestos/buscar?q=taladro — Búsqueda semántica por texto. */
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

    /** GET /api/semantico/clientes/buscar?nombre=juan — Buscar clientes por nombre. */
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

    /** GET /api/semantico/ventas/cliente?nombre=juan — Ventas filtradas por nombre de cliente. */
    @GetMapping("/ventas/cliente")
    public ResponseEntity<List<Map<String, String>>> buscarVentasPorCliente(@RequestParam String nombre) {
        List<Map<String, String>> resultado = sparqlRepository.buscarVentasPorCliente(nombre);
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
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

    /** GET /api/semantico/detalles/venta/{ventaId} — Detalles de una venta específica. */
    @GetMapping("/detalles/venta/{ventaId}")
    public ResponseEntity<List<Map<String, String>>> detallesPorVenta(@PathVariable String ventaId) {
        List<Map<String, String>> resultado = sparqlRepository.buscarDetallesPorVenta(ventaId);
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
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

    /** GET /api/semantico/pagos/estado/{estado} — Filtrar por estado (PAGADO, PENDIENTE, ANULADO). */
    @GetMapping("/pagos/estado/{estado}")
    public ResponseEntity<List<Map<String, String>>> buscarPagosPorEstado(@PathVariable String estado) {
        List<Map<String, String>> resultado = sparqlRepository.buscarPagosPorEstado(estado.toUpperCase());
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
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

    /** GET /api/semantico/movimientos/tipo/{tipo} — Filtrar por tipo (VENTA, ENTRADA, DEVOLUCION). */
    @GetMapping("/movimientos/tipo/{tipo}")
    public ResponseEntity<List<Map<String, String>>> buscarMovimientosPorTipo(@PathVariable String tipo) {
        List<Map<String, String>> resultado = sparqlRepository.buscarMovimientosPorTipo(tipo.toUpperCase());
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
    }

    /** GET /api/semantico/movimientos/repuesto?nombre=taladro — Filtrar por nombre de repuesto. */
    @GetMapping("/movimientos/repuesto")
    public ResponseEntity<List<Map<String, String>>> buscarMovimientosPorRepuesto(@RequestParam String nombre) {
        List<Map<String, String>> resultado = sparqlRepository.buscarMovimientosPorRepuesto(nombre);
        if (resultado.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(resultado);
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
        if (query == null || query.isBlank()) return ResponseEntity.badRequest().build();
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

