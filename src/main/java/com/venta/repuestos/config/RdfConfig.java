package com.venta.repuestos.config;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.system.Txn;
import org.apache.jena.tdb2.TDB2Factory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

/**
 * Configuración de Apache Jena para Web Semántica.
 *
 * Responsabilidades:
 *  - Crea el Dataset TDB2 en memoria.
 *  - Carga ÚNICAMENTE el esquema OWL/SKOS desde repuestos_ontology.ttl
 *    (clases, propiedades y taxonomías; sin instancias hardcodeadas).
 *
 * Las instancias de datos (Repuesto, Cliente) son inyectadas dinámicamente
 * por OntologyLoaderService al arrancar y cuando se solicite recarga.
 */
@Configuration
public class RdfConfig {

    @Bean
    public Dataset rdfDataset() {
        Dataset dataset = TDB2Factory.createDataset();

        try {
            InputStream in = getClass().getResourceAsStream("/ontology/repuestos_ontology.ttl");
            if (in != null) {
                Txn.executeWrite(dataset, () -> {
                    RDFDataMgr.read(dataset, in, org.apache.jena.riot.Lang.TURTLE);
                });
                System.out.println("[RDF] Esquema OWL/SKOS cargado en el Dataset RDF correctamente.");
                System.out.println("[RDF] Las instancias de datos se cargarán desde la BD vía OntologyLoaderService.");
            } else {
                System.err.println("[RDF] ERROR: No se encontró repuestos_ontology.ttl");
            }
        } catch (Exception e) {
            System.err.println("[RDF] Error al cargar el esquema OWL: " + e.getMessage());
        }

        return dataset;
    }

    @Bean
    public RDFConnection rdfConnection(Dataset rdfDataset) {
        return RDFConnection.connect(rdfDataset);
    }
}
