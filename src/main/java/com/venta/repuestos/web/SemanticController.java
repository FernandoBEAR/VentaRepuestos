package com.venta.repuestos.web;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.repositorios.SparqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la capa de Web Semántica.
 * Expone endpoints que consultan el grafo RDF mediante SPARQL.
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

    /**
     * GET /api/semantico/repuestos
     * Retorna todos los repuestos del grafo RDF.
     */
    @GetMapping("/repuestos")
    public ResponseEntity<List<RepuestoDTO>> listarRepuestos() {
        List<RepuestoDTO> repuestos = sparqlRepository.buscarTodosRepuestos();
        return ResponseEntity.ok(repuestos);
    }

    /**
     * GET /api/semantico/repuestos/marca/{marca}
     * Retorna repuestos filtrados por marca usando SKOS.
     * Ejemplo: /api/semantico/repuestos/marca/BOSCH
     */
    @GetMapping("/repuestos/marca/{marca}")
    public ResponseEntity<List<RepuestoDTO>> buscarPorMarca(@PathVariable String marca) {
        List<RepuestoDTO> repuestos = sparqlRepository.buscarRepuestosPorMarca(marca);
        if (repuestos.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(repuestos);
    }

    /**
     * GET /api/semantico/clientes
     * Retorna todos los clientes del grafo RDF.
     */
    @GetMapping("/clientes")
    public ResponseEntity<List<Map<String, String>>> listarClientes() {
        List<Map<String, String>> clientes = sparqlRepository.buscarClientes();
        return ResponseEntity.ok(clientes);
    }

    /**
     * POST /api/semantico/repuestos
     * Inserta un nuevo repuesto al grafo RDF como tripleta.
     */
    @PostMapping("/repuestos")
    public ResponseEntity<String> insertarRepuesto(@RequestBody RepuestoDTO repuestoDTO) {
        boolean ok = sparqlRepository.insertarRepuesto(repuestoDTO);
        if (ok) {
            return ResponseEntity.ok("Repuesto insertado en el grafo RDF exitosamente.");
        }
        return ResponseEntity.internalServerError().body("Error al insertar el repuesto en el grafo RDF.");
    }

    /**
     * POST /api/semantico/sparql
     * Ejecuta una consulta SPARQL libre contra el grafo.
     * Body: { "query": "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 10" }
     */
    @PostMapping("/sparql")
    public ResponseEntity<List<Map<String, String>>> ejecutarSparql(@RequestBody Map<String, String> body) {
        String query = body.get("query");
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            List<Map<String, String>> resultados = sparqlRepository.ejecutarConsultaLibre(query);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
