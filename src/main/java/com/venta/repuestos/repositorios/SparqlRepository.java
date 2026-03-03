package com.venta.repuestos.repositorios;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.enums.Marca;
import org.apache.jena.query.*;
import org.apache.jena.update.*;
import org.apache.jena.system.Txn;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repositorio que ejecuta consultas SPARQL contra el Dataset RDF local.
 *
 * Los datos son cargados dinámicamente desde la base de datos por
 * {@link com.venta.repuestos.servicios.OntologyLoaderService} al arrancar.
 *
 * Entidades consultables: Repuesto, Cliente, Venta, DetalleVenta, Pago,
 * Movimiento.
 */
@Repository
public class SparqlRepository {

    private final Dataset dataset;

    private static final String PREFIXES = "PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
            "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "PREFIX owl:    <http://www.w3.org/2002/07/owl#>\n" +
            "PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>\n" +
            "PREFIX skos:   <http://www.w3.org/2004/02/skos/core#>\n" +
            "PREFIX schema: <http://schema.org/>\n" +
            "PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX gr:     <http://purl.org/goodrelations/v1#>\n" +
            "PREFIX vr:     <http://www.ventarepuestos.com/ontology#>\n";

    public SparqlRepository(Dataset dataset) {
        this.dataset = dataset;
    }

    // =========================================================================
    // REPUESTOS
    // =========================================================================

    /** Retorna todos los repuestos sincronizados desde la BD. */
    public List<RepuestoDTO> buscarTodosRepuestos() {
        String sparql = PREFIXES +
                "SELECT ?nombre ?descripcion ?precio ?stock ?marcaLabel\n" +
                "WHERE {\n" +
                "  ?rep a vr:Repuesto ; schema:name ?nombre ; vr:precio ?precio .\n" +
                "  OPTIONAL { ?rep vr:descripcion ?descripcion }\n" +
                "  OPTIONAL { ?rep vr:stock ?stock }\n" +
                "  OPTIONAL { ?rep vr:tieneMarca ?marca . ?marca skos:prefLabel ?marcaLabel }\n" +
                "} ORDER BY ?nombre";
        return ejecutarConsultaRepuestos(sparql);
    }

    /** Filtra repuestos por marca usando la taxonomía SKOS. Ej: "BOSCH" */
    public List<RepuestoDTO> buscarRepuestosPorMarca(String marcaLabel) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?nombre ?descripcion ?precio ?stock ?marcaLabel\n" +
                "WHERE {\n" +
                "  ?rep a vr:Repuesto ; schema:name ?nombre ; vr:precio ?precio ;\n" +
                "       vr:tieneMarca ?marca .\n" +
                "  ?marca skos:prefLabel ?marcaLabel .\n" +
                "  OPTIONAL { ?rep vr:descripcion ?descripcion }\n" +
                "  OPTIONAL { ?rep vr:stock ?stock }\n" +
                "  FILTER(UCASE(STR(?marcaLabel)) = UCASE(?filtroMarca))\n" +
                "} ORDER BY ?nombre");
        pss.setLiteral("filtroMarca", marcaLabel);
        return ejecutarConsultaRepuestos(pss.toString());
    }

    /** Búsqueda semántica de repuestos por texto libre en nombre o descripción. */
    public List<RepuestoDTO> buscarRepuestosPorTexto(String texto) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?nombre ?descripcion ?precio ?stock ?marcaLabel\n" +
                "WHERE {\n" +
                "  ?rep a vr:Repuesto ; schema:name ?nombre ; vr:precio ?precio .\n" +
                "  OPTIONAL { ?rep vr:descripcion ?descripcion }\n" +
                "  OPTIONAL { ?rep vr:stock ?stock }\n" +
                "  OPTIONAL { ?rep vr:tieneMarca ?marca . ?marca skos:prefLabel ?marcaLabel }\n" +
                "  FILTER(\n" +
                "    CONTAINS(LCASE(STR(?nombre)), LCASE(?q))\n" +
                "    || (BOUND(?descripcion) && CONTAINS(LCASE(STR(?descripcion)), LCASE(?q)))\n" +
                "  )\n" +
                "} ORDER BY ?nombre");
        pss.setLiteral("q", texto);
        return ejecutarConsultaRepuestos(pss.toString());
    }

    // =========================================================================
    // CLIENTES
    // =========================================================================

    /** Retorna todos los clientes del grafo RDF. */
    public List<Map<String, String>> buscarClientes() {
        String sparql = PREFIXES +
                "SELECT ?id ?nombre ?email\n" +
                "WHERE {\n" +
                "  ?cliente a vr:Cliente ; foaf:name ?nombre .\n" +
                "  OPTIONAL { ?cliente vr:email ?email }\n" +
                "  BIND(STRAFTER(STR(?cliente), \"ontology#cliente_\") AS ?id)\n" +
                "} ORDER BY ?nombre";
        return ejecutarConsultaLibre(sparql);
    }

    /**
     * Inserta un repuesto como tripleta RDF en el grafo.
     */
    public boolean insertarRepuesto(RepuestoDTO dto) {
        String marcaUri = "vr:Marca" + (dto.getMarca() != null
                ? dto.getMarca().name().charAt(0) + dto.getMarca().name().substring(1).toLowerCase()
                : "Desconocida");

        // Anclamos la URI de RDF al identificador Autoincremental de MySQL
        String nodeId = "vr:repuesto_mysql_" + dto.getId();

        String sparql = PREFIXES +
                "INSERT DATA {\n" +
                "  " + nodeId + " a vr:Repuesto ;\n" +
                "    schema:name \"" + escapeSparql(dto.getNombre()) + "\" ;\n" +
                "    vr:precio " + (dto.getPrecio() != null ? dto.getPrecio() : 0.0) + " ;\n" +
                "    vr:stock " + (dto.getStock() != null ? dto.getStock() : 0) + " ;\n" +
                (dto.getDescripcion() != null ? "    vr:descripcion \"" + escapeSparql(dto.getDescripcion()) + "\" ;\n"
                        : "")
                +
                "    vr:tieneMarca " + marcaUri + " .\n" +
                "}";

        try {
            Txn.executeWrite(dataset, () -> {
                org.apache.jena.update.UpdateProcessor proc = org.apache.jena.update.UpdateExecutionFactory
                        .create(org.apache.jena.update.UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error insertando repuesto: " + e.getMessage());
            return false;
        }
    }

    /** Busca clientes por nombre (case-insensitive). */
    public List<Map<String, String>> buscarClientesPorNombre(String nombre) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?id ?nombre ?email\n" +
                "WHERE {\n" +
                "  ?cliente a vr:Cliente ; foaf:name ?nombre .\n" +
                "  OPTIONAL { ?cliente vr:email ?email }\n" +
                "  BIND(STRAFTER(STR(?cliente), \"ontology#cliente_\") AS ?id)\n" +
                "  FILTER(CONTAINS(LCASE(STR(?nombre)), LCASE(?filtroNombre)))\n" +
                "} ORDER BY ?nombre");
        pss.setLiteral("filtroNombre", nombre);
        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // VENTAS
    // =========================================================================

    /** Retorna todas las ventas con su cliente y total. */
    public List<Map<String, String>> buscarVentas() {
        String sparql = PREFIXES +
                "SELECT ?ventaId ?total ?clienteNombre\n" +
                "WHERE {\n" +
                "  ?venta a vr:Venta .\n" +
                "  OPTIONAL { ?venta vr:totalVenta ?total }\n" +
                "  OPTIONAL { ?venta vr:tieneCliente ?cli . ?cli foaf:name ?clienteNombre }\n" +
                "  BIND(STRAFTER(STR(?venta), \"ontology#venta_\") AS ?ventaId)\n" +
                "} ORDER BY ?ventaId";
        return ejecutarConsultaLibre(sparql);
    }

    /** Retorna todas las ventas de un cliente específico por nombre. */
    public List<Map<String, String>> buscarVentasPorCliente(String nombreCliente) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?ventaId ?total ?clienteNombre\n" +
                "WHERE {\n" +
                "  ?venta a vr:Venta ;\n" +
                "         vr:tieneCliente ?cli .\n" +
                "  ?cli foaf:name ?clienteNombre .\n" +
                "  OPTIONAL { ?venta vr:totalVenta ?total }\n" +
                "  BIND(STRAFTER(STR(?venta), \"ontology#venta_\") AS ?ventaId)\n" +
                "  FILTER(CONTAINS(LCASE(STR(?clienteNombre)), LCASE(?filtroCliente)))\n" +
                "} ORDER BY ?ventaId");
        pss.setLiteral("filtroCliente", nombreCliente);
        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // DETALLE VENTA
    // =========================================================================

    /** Retorna todos los detalles de venta con repuesto, cantidad y subtotal. */
    public List<Map<String, String>> buscarDetallesVenta() {
        String sparql = PREFIXES +
                "SELECT ?detalleId ?ventaId ?repuestoNombre ?cantidad ?precioUnitario ?subtotal\n" +
                "WHERE {\n" +
                "  ?detalle a vr:DetalleVenta ; vr:cantidadDetalle ?cantidad .\n" +
                "  OPTIONAL { ?detalle vr:precioUnitario ?precioUnitario }\n" +
                "  OPTIONAL { ?detalle vr:subtotal ?subtotal }\n" +
                "  OPTIONAL { ?detalle vr:detalleRepuesto ?rep . ?rep schema:name ?repuestoNombre }\n" +
                "  OPTIONAL { ?venta vr:tieneDetalle ?detalle .\n" +
                "             BIND(STRAFTER(STR(?venta), \"ontology#venta_\") AS ?ventaId) }\n" +
                "  BIND(STRAFTER(STR(?detalle), \"ontology#detalle_\") AS ?detalleId)\n" +
                "} ORDER BY ?ventaId";
        return ejecutarConsultaLibre(sparql);
    }

    /** Retorna los detalles de una venta específica por ID de venta. */
    public List<Map<String, String>> buscarDetallesPorVenta(String ventaId) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?detalleId ?repuestoNombre ?cantidad ?precioUnitario ?subtotal\n" +
                "WHERE {\n" +
                "  ?venta vr:tieneDetalle ?detalle .\n" +
                "  ?detalle a vr:DetalleVenta ; vr:cantidadDetalle ?cantidad .\n" +
                "  OPTIONAL { ?detalle vr:precioUnitario ?precioUnitario }\n" +
                "  OPTIONAL { ?detalle vr:subtotal ?subtotal }\n" +
                "  OPTIONAL { ?detalle vr:detalleRepuesto ?rep . ?rep schema:name ?repuestoNombre }\n" +
                "  BIND(STRAFTER(STR(?detalle), \"ontology#detalle_\") AS ?detalleId)\n" +
                "  FILTER(STRAFTER(STR(?venta), \"ontology#venta_\") = ?filtroVenta)\n" +
                "}");
        pss.setLiteral("filtroVenta", ventaId);
        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // PAGOS
    // =========================================================================

    /** Retorna todos los pagos con su estado, monto y método de pago. */
    public List<Map<String, String>> buscarPagos() {
        String sparql = PREFIXES +
                "SELECT ?pagoId ?monto ?estado ?comprobante ?fecha ?metodoPago\n" +
                "WHERE {\n" +
                "  ?pago a vr:Pago ; vr:montoPago ?monto .\n" +
                "  OPTIONAL { ?pago vr:estadoPago ?estado }\n" +
                "  OPTIONAL { ?pago vr:tipoComprobante ?comprobante }\n" +
                "  OPTIONAL { ?pago vr:fechaPago ?fecha }\n" +
                "  OPTIONAL { ?pago vr:tieneMetodoPago ?met . ?met skos:prefLabel ?metodoPago }\n" +
                "  BIND(STRAFTER(STR(?pago), \"ontology#pago_\") AS ?pagoId)\n" +
                "} ORDER BY ?pagoId";
        return ejecutarConsultaLibre(sparql);
    }

    /** Filtra pagos por estado. Ej: "PAGADO", "PENDIENTE" */
    public List<Map<String, String>> buscarPagosPorEstado(String estado) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?pagoId ?monto ?estado ?comprobante ?fecha ?metodoPago\n" +
                "WHERE {\n" +
                "  ?pago a vr:Pago ; vr:montoPago ?monto ; vr:estadoPago ?estado .\n" +
                "  OPTIONAL { ?pago vr:tipoComprobante ?comprobante }\n" +
                "  OPTIONAL { ?pago vr:fechaPago ?fecha }\n" +
                "  OPTIONAL { ?pago vr:tieneMetodoPago ?met . ?met skos:prefLabel ?metodoPago }\n" +
                "  BIND(STRAFTER(STR(?pago), \"ontology#pago_\") AS ?pagoId)\n" +
                "  FILTER(UCASE(STR(?estado)) = UCASE(?filtroEstado))\n" +
                "} ORDER BY ?pagoId");
        pss.setLiteral("filtroEstado", estado);
        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // MOVIMIENTOS
    // =========================================================================

    /** Retorna todos los movimientos de inventario. */
    public List<Map<String, String>> buscarMovimientos() {
        String sparql = PREFIXES +
                "SELECT ?movId ?repuestoNombre ?tipoMovimiento ?cantidad ?dia ?mes ?anio\n" +
                "WHERE {\n" +
                "  ?mov a vr:Movimiento ; vr:cantidadMovimiento ?cantidad .\n" +
                "  OPTIONAL { ?mov vr:movimientoRepuesto ?rep . ?rep schema:name ?repuestoNombre }\n" +
                "  OPTIONAL { ?mov vr:tieneTipoMovimiento ?tipo . ?tipo skos:prefLabel ?tipoMovimiento }\n" +
                "  OPTIONAL { ?mov vr:diaMovimiento ?dia }\n" +
                "  OPTIONAL { ?mov vr:mesMovimiento ?mes }\n" +
                "  OPTIONAL { ?mov vr:anioMovimiento ?anio }\n" +
                "  BIND(STRAFTER(STR(?mov), \"ontology#movimiento_\") AS ?movId)\n" +
                "} ORDER BY ?anio ?mes ?dia";
        return ejecutarConsultaLibre(sparql);
    }

    /** Filtra movimientos por tipo. Ej: "VENTA", "ENTRADA", "DEVOLUCION" */
    public List<Map<String, String>> buscarMovimientosPorTipo(String tipo) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?movId ?repuestoNombre ?tipoMovimiento ?cantidad ?dia ?mes ?anio\n" +
                "WHERE {\n" +
                "  ?mov a vr:Movimiento ; vr:cantidadMovimiento ?cantidad ;\n" +
                "       vr:tieneTipoMovimiento ?tipoRef .\n" +
                "  ?tipoRef skos:prefLabel ?tipoMovimiento .\n" +
                "  OPTIONAL { ?mov vr:movimientoRepuesto ?rep . ?rep schema:name ?repuestoNombre }\n" +
                "  OPTIONAL { ?mov vr:diaMovimiento ?dia }\n" +
                "  OPTIONAL { ?mov vr:mesMovimiento ?mes }\n" +
                "  OPTIONAL { ?mov vr:anioMovimiento ?anio }\n" +
                "  BIND(STRAFTER(STR(?mov), \"ontology#movimiento_\") AS ?movId)\n" +
                "  FILTER(UCASE(STR(?tipoMovimiento)) = UCASE(?filtroTipo))\n" +
                "} ORDER BY ?anio ?mes ?dia");
        pss.setLiteral("filtroTipo", tipo);
        return ejecutarConsultaLibre(pss.toString());
    }

    /** Filtra movimientos por nombre de repuesto (case-insensitive). */
    public List<Map<String, String>> buscarMovimientosPorRepuesto(String nombreRepuesto) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?movId ?repuestoNombre ?tipoMovimiento ?cantidad ?dia ?mes ?anio\n" +
                "WHERE {\n" +
                "  ?mov a vr:Movimiento ; vr:cantidadMovimiento ?cantidad ;\n" +
                "       vr:movimientoRepuesto ?rep .\n" +
                "  ?rep schema:name ?repuestoNombre .\n" +
                "  OPTIONAL { ?mov vr:tieneTipoMovimiento ?tipo . ?tipo skos:prefLabel ?tipoMovimiento }\n" +
                "  OPTIONAL { ?mov vr:diaMovimiento ?dia }\n" +
                "  OPTIONAL { ?mov vr:mesMovimiento ?mes }\n" +
                "  OPTIONAL { ?mov vr:anioMovimiento ?anio }\n" +
                "  BIND(STRAFTER(STR(?mov), \"ontology#movimiento_\") AS ?movId)\n" +
                "  FILTER(CONTAINS(LCASE(STR(?repuestoNombre)), LCASE(?filtroRep)))\n" +
                "} ORDER BY ?anio ?mes ?dia");
        pss.setLiteral("filtroRep", nombreRepuesto);
        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // CONSULTA MULTI-SALTO SEMÁNTICA
    // =========================================================================

    /**
     * Consulta multi-salto: "Dame todos los repuestos de una marca X,
     * vendidos a clientes que pagaron con un metodo".
     *
     * Recorrido del grafo (4 saltos):
     *   Repuesto <--[detalleRepuesto]-- DetalleVenta
     *   DetalleVenta <--[tieneDetalle]-- Venta
     *   Venta --[tieneCliente]--> Cliente
     *   Venta --[tienePago]--> Pago --[tieneMetodoPago]--> ConceptoSKOS (prefLabel = metodo
     *
     * @param marca      Marca exacta (BOSCH, STANLEY, TRUPPER). Null = todas.
     * @param metodoPago Metodo de pago Efectivo, tarjeta , etc
     */
    public List<Map<String, String>> buscarRepuestosVendidosPorMarcaYMetodoPago(String marca, String metodoPago) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();

        StringBuilder filtros = new StringBuilder();
        if (marca != null && !marca.isBlank()) {
            filtros.append("  FILTER(UCASE(STR(?marcaLabel)) = UCASE(?filtroMarca))\n");
        }
        if (metodoPago != null && !metodoPago.isBlank()) {
            filtros.append("  FILTER(UCASE(STR(?metodoPagoLabel)) = UCASE(?filtroMetodo))\n");
        }

        pss.setCommandText(PREFIXES +
                "SELECT DISTINCT ?repuestoNombre ?marcaLabel ?precio ?stock\n" +
                "                ?clienteNombre ?metodoPagoLabel ?ventaId ?subtotal\n" +
                "WHERE {\n" +
                // Salto 1: Repuesto con nombre y precio
                "  ?rep a vr:Repuesto ;\n" +
                "       schema:name ?repuestoNombre ;\n" +
                "       vr:precio ?precio .\n" +
                "  OPTIONAL { ?rep vr:stock ?stock }\n" +
                // Salto 2: Repuesto → tieneMarca → ConceptoSKOS
                "  ?rep vr:tieneMarca ?marca .\n" +
                "  ?marca skos:prefLabel ?marcaLabel .\n" +
                // Salto 3: DetalleVenta → detalleRepuesto → Repuesto
                "  ?detalle vr:detalleRepuesto ?rep .\n" +
                "  OPTIONAL { ?detalle vr:subtotal ?subtotal }\n" +
                // Salto 4: Venta → tieneDetalle → DetalleVenta
                "  ?venta vr:tieneDetalle ?detalle .\n" +
                "  BIND(STRAFTER(STR(?venta), \"ontology#venta_\") AS ?ventaId)\n" +
                // Salto 5a: Venta → tieneCliente → Cliente
                "  ?venta vr:tieneCliente ?cli .\n" +
                "  ?cli foaf:name ?clienteNombre .\n" +
                // Salto 5b: Venta → tienePago → Pago → tieneMetodoPago → ConceptoSKOS
                "  ?venta vr:tienePago ?pago .\n" +
                "  ?pago vr:tieneMetodoPago ?metodo .\n" +
                "  ?metodo skos:prefLabel ?metodoPagoLabel .\n" +
                filtros +
                "} ORDER BY ?repuestoNombre ?clienteNombre");

        if (marca != null && !marca.isBlank()) {
            pss.setLiteral("filtroMarca", marca.toUpperCase());
        }
        if (metodoPago != null && !metodoPago.isBlank()) {
            pss.setLiteral("filtroMetodo", metodoPago.toUpperCase());
        }

        return ejecutarConsultaLibre(pss.toString());
    }

    // =========================================================================
    // CONSULTA LIBRE
    // =========================================================================

    /**
     * <<<<<<< HEAD
     * Actualiza un repuesto en el Grafo RDF sobreescribiendo sus datos actuales.
     */
    public boolean actualizarRepuesto(RepuestoDTO dto) {
        String nodeId = "vr:repuesto_mysql_" + dto.getId();
        String marcaUri = "vr:Marca" + (dto.getMarca() != null
                ? dto.getMarca().name().charAt(0) + dto.getMarca().name().substring(1).toLowerCase()
                : "Desconocida");

        // Primero borramos todas las propiedades que apuntan desde ese nodo
        // y luego reinsertamos las nuevas usando DELETE / INSERT en SPARQL Update
        String sparql = PREFIXES +
                "DELETE { " + nodeId + " ?p ?o } \n" +
                "WHERE  { " + nodeId + " ?p ?o } ;\n" +
                "INSERT DATA {\n" +
                "  " + nodeId + " a vr:Repuesto ;\n" +
                "    schema:name \"" + escapeSparql(dto.getNombre()) + "\" ;\n" +
                "    vr:precio " + (dto.getPrecio() != null ? dto.getPrecio() : 0.0) + " ;\n" +
                "    vr:stock " + (dto.getStock() != null ? dto.getStock() : 0) + " ;\n" +
                (dto.getDescripcion() != null ? "    vr:descripcion \"" + escapeSparql(dto.getDescripcion()) + "\" ;\n"
                        : "")
                +
                "    vr:tieneMarca " + marcaUri + " .\n" +
                "}";

        try {
            Txn.executeWrite(dataset, () -> {
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error actualizando repuesto en RDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina completamente un nodo de repuesto del grafo RDF.
     */
    public boolean eliminarRepuesto(Long id) {
        String nodeId = "vr:repuesto_mysql_" + id;
        String sparql = PREFIXES +
                "DELETE { " + nodeId + " ?p ?o } \n" +
                "WHERE  { " + nodeId + " ?p ?o }";

        try {
            Txn.executeWrite(dataset, () -> {
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error eliminando repuesto en RDF: " + e.getMessage());
            return false;
        }
    }

    public boolean insertarCliente(com.venta.repuestos.entidades.Cliente dto) {
        String nodeId = "vr:cliente_mysql_" + dto.getId();
        String sparql = PREFIXES +
                "INSERT DATA {\n" +
                "  " + nodeId + " a vr:Cliente ;\n" +
                "    foaf:name \"" + escapeSparql(dto.getNombre()) + "\" " +
                (dto.getEmail() != null ? ";\n    vr:email \"" + escapeSparql(dto.getEmail()) + "\" .\n" : ".\n") +
                "}";
        try {
            Txn.executeWrite(dataset, () -> {
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error insertando cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarCliente(com.venta.repuestos.entidades.Cliente dto) {
        String nodeId = "vr:cliente_mysql_" + dto.getId();
        String sparql = PREFIXES +
                "DELETE { " + nodeId + " ?p ?o } \n" +
                "WHERE  { " + nodeId + " ?p ?o } ;\n" +
                "INSERT DATA {\n" +
                "  " + nodeId + " a vr:Cliente ;\n" +
                "    foaf:name \"" + escapeSparql(dto.getNombre()) + "\" " +
                (dto.getEmail() != null ? ";\n    vr:email \"" + escapeSparql(dto.getEmail()) + "\" .\n" : ".\n") +
                "}";
        try {
            Txn.executeWrite(dataset, () -> {
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error actualizando cliente en RDF: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarCliente(Long id) {
        String nodeId = "vr:cliente_mysql_" + id;
        String sparql = PREFIXES +
                "DELETE { " + nodeId + " ?p ?o } \n" +
                "WHERE  { " + nodeId + " ?p ?o }";
        try {
            Txn.executeWrite(dataset, () -> {
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error eliminando cliente en RDF: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ejecuta una consulta SPARQL libre y devuelve resultados como lista de mapas.
     * Los prefijos estándar se añaden automáticamente al inicio.
     */
    public List<Map<String, String>> ejecutarConsultaLibre(String consulta) {
        String fullQuery = consulta.trim().toUpperCase().startsWith("PREFIX")
                ? consulta
                : PREFIXES + consulta;

        List<Map<String, String>> resultados = new ArrayList<>();
        Txn.executeRead(dataset, () -> {
            try (QueryExecution qe = QueryExecutionFactory.create(fullQuery, dataset)) {
                ResultSet rs = qe.execSelect();
                List<String> vars = rs.getResultVars();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    Map<String, String> fila = new LinkedHashMap<>();
                    for (String var : vars) {
                        if (sol.contains(var)) {
                            fila.put(var, sol.get(var).isLiteral()
                                    ? sol.getLiteral(var).getString()
                                    : sol.get(var).toString());
                        }
                    }
                    resultados.add(fila);
                }
            }
        });
        return resultados;
    }

    // =========================================================================
    // AUXILIARES
    // =========================================================================

    private List<RepuestoDTO> ejecutarConsultaRepuestos(String sparql) {
        List<RepuestoDTO> repuestos = new ArrayList<>();
        Txn.executeRead(dataset, () -> {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    RepuestoDTO dto = new RepuestoDTO();
                    if (sol.contains("nombre"))
                        dto.setNombre(sol.getLiteral("nombre").getString());
                    if (sol.contains("descripcion"))
                        dto.setDescripcion(sol.getLiteral("descripcion").getString());
                    if (sol.contains("precio"))
                        dto.setPrecio(sol.getLiteral("precio").getDouble());
                    if (sol.contains("stock"))
                        dto.setStock(sol.getLiteral("stock").getInt());
                    if (sol.contains("marcaLabel")) {
                        try {
                            dto.setMarca(Marca.valueOf(
                                    sol.getLiteral("marcaLabel").getString().toUpperCase()));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    repuestos.add(dto);
                }
            }
        });
        return repuestos;
    }

    private String escapeSparql(String value) {
        if (value == null)
            return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
