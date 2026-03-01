package com.venta.repuestos.repositorios;

import com.venta.repuestos.dtos.RepuestoDTO;
import com.venta.repuestos.enums.Marca;
import org.apache.jena.query.*;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repositorio que ejecuta consultas SPARQL contra el Dataset RDF local.
 * Todas las operaciones usan transacciones Txn para compatibilidad con TDB2.
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
            "PREFIX gr:     <http://purl.org/goodrelations/v1#>\n" +
            "PREFIX foaf:   <http://xmlns.com/foaf/0.1/>\n" +
            "PREFIX vr:     <http://www.ventarepuestos.com/ontology#>\n";

    public SparqlRepository(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * Busca todos los repuestos cargados en el grafo RDF.
     */
    public List<RepuestoDTO> buscarTodosRepuestos() {
        String sparql = PREFIXES +
                "SELECT ?nombre ?descripcion ?precio ?stock ?marcaLabel\n" +
                "WHERE {\n" +
                "  ?repuesto a vr:Repuesto ;\n" +
                "            schema:name ?nombre ;\n" +
                "            vr:precio ?precio .\n" +
                "  OPTIONAL { ?repuesto vr:descripcion ?descripcion }\n" +
                "  OPTIONAL { ?repuesto vr:stock ?stock }\n" +
                "  OPTIONAL { ?repuesto vr:tieneMarca ?marca . ?marca skos:prefLabel ?marcaLabel }\n" +
                "}";

        return ejecutarConsultaRepuestos(sparql);
    }

    /**
     * Busca repuestos filtrados por marca usando SKOS.
     */
    public List<RepuestoDTO> buscarRepuestosPorMarca(String marcaLabel) {
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        pss.setCommandText(PREFIXES +
                "SELECT ?nombre ?descripcion ?precio ?stock ?marcaLabel\n" +
                "WHERE {\n" +
                "  ?repuesto a vr:Repuesto ;\n" +
                "            schema:name ?nombre ;\n" +
                "            vr:precio ?precio ;\n" +
                "            vr:tieneMarca ?marca .\n" +
                "  ?marca skos:prefLabel ?marcaLabel .\n" +
                "  OPTIONAL { ?repuesto vr:descripcion ?descripcion }\n" +
                "  OPTIONAL { ?repuesto vr:stock ?stock }\n" +
                "  FILTER(?marcaLabel = ?filtroMarca)\n" +
                "}");
        pss.setLiteral("filtroMarca", marcaLabel);

        return ejecutarConsultaRepuestos(pss.toString());
    }

    /**
     * Busca todos los clientes cargados en el grafo RDF.
     */
    public List<Map<String, String>> buscarClientes() {
        String sparql = PREFIXES +
                "SELECT ?nombre ?email\n" +
                "WHERE {\n" +
                "  ?cliente a vr:Cliente ;\n" +
                "           foaf:name ?nombre .\n" +
                "  OPTIONAL { ?cliente vr:email ?email }\n" +
                "}";

        List<Map<String, String>> clientes = new ArrayList<>();
        Txn.executeRead(dataset, () -> {
            try (QueryExecution qe = QueryExecutionFactory.create(sparql, dataset)) {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    QuerySolution sol = rs.next();
                    Map<String, String> cliente = new LinkedHashMap<>();
                    cliente.put("nombre", sol.contains("nombre") ? sol.getLiteral("nombre").getString() : null);
                    cliente.put("email", sol.contains("email") ? sol.getLiteral("email").getString() : null);
                    clientes.add(cliente);
                }
            }
        });
        return clientes;
    }

    /**
     * Inserta un repuesto como tripleta RDF en el grafo.
     */
    public boolean insertarRepuesto(RepuestoDTO dto) {
        String marcaUri = "vr:Marca" + (dto.getMarca() != null
                ? dto.getMarca().name().charAt(0) + dto.getMarca().name().substring(1).toLowerCase()
                : "Desconocida");
        String nodeId = "vr:repuesto_" + System.currentTimeMillis();

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
                UpdateProcessor proc = UpdateExecutionFactory.create(UpdateFactory.create(sparql), dataset);
                proc.execute();
            });
            return true;
        } catch (Exception e) {
            System.err.println("[SPARQL] Error insertando repuesto: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ejecuta una consulta SPARQL libre y devuelve los resultados como lista de
     * mapas.
     */
    public List<Map<String, String>> ejecutarConsultaLibre(String consulta) {
        String fullQuery = PREFIXES + consulta;
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
                            if (sol.get(var).isLiteral()) {
                                fila.put(var, sol.getLiteral(var).getString());
                            } else {
                                fila.put(var, sol.get(var).toString());
                            }
                        }
                    }
                    resultados.add(fila);
                }
            }
        });

        return resultados;
    }

    // --- Métodos auxiliares ---

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
                            dto.setMarca(Marca.valueOf(sol.getLiteral("marcaLabel").getString()));
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
