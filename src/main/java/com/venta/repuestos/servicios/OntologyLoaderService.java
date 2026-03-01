package com.venta.repuestos.servicios;

import com.venta.repuestos.entidades.*;
import com.venta.repuestos.repositorios.*;
import jakarta.annotation.PostConstruct;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.system.Txn;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio responsable de sincronizar TODOS los datos de la base de datos
 * con el grafo RDF en memoria (Dataset TDB2).
 *
 * Entidades sincronizadas:
 *  - Repuesto, Cliente, Venta, DetalleVenta, Pago, Movimiento
 *
 * Flujo:
 *  1. Al arrancar (@PostConstruct), limpia las instancias del grafo.
 *  2. Consulta cada entidad desde la BD vía JPA.
 *  3. Inserta cada instancia como tripletas RDF en el Dataset.
 *  4. Expone recargar() para sincronizar nuevamente cuando la BD cambie.
 */
@Service
public class OntologyLoaderService {

    private static final Logger log = LoggerFactory.getLogger(OntologyLoaderService.class);

    // ─── URIs base ────────────────────────────────────────────────────────────
    private static final String BASE_URI   = "http://www.ventarepuestos.com/ontology#";
    private static final String SCHEMA_URI = "http://schema.org/";
    private static final String FOAF_URI   = "http://xmlns.com/foaf/0.1/";

    // ─── Clases RDF ───────────────────────────────────────────────────────────
    private static final Resource CLASS_REPUESTO     = ResourceFactory.createResource(BASE_URI + "Repuesto");
    private static final Resource CLASS_CLIENTE      = ResourceFactory.createResource(BASE_URI + "Cliente");
    private static final Resource CLASS_VENTA        = ResourceFactory.createResource(BASE_URI + "Venta");
    private static final Resource CLASS_PAGO         = ResourceFactory.createResource(BASE_URI + "Pago");
    private static final Resource CLASS_DETALLE      = ResourceFactory.createResource(BASE_URI + "DetalleVenta");
    private static final Resource CLASS_MOVIMIENTO   = ResourceFactory.createResource(BASE_URI + "Movimiento");

    // ─── Propiedades Repuesto ─────────────────────────────────────────────────
    private static final Property PROP_NAME          = ResourceFactory.createProperty(SCHEMA_URI, "name");
    private static final Property PROP_DESCRIPCION   = ResourceFactory.createProperty(BASE_URI, "descripcion");
    private static final Property PROP_PRECIO        = ResourceFactory.createProperty(BASE_URI, "precio");
    private static final Property PROP_STOCK         = ResourceFactory.createProperty(BASE_URI, "stock");
    private static final Property PROP_TIENE_MARCA   = ResourceFactory.createProperty(BASE_URI, "tieneMarca");

    // ─── Propiedades Cliente ──────────────────────────────────────────────────
    private static final Property PROP_FOAF_NAME     = ResourceFactory.createProperty(FOAF_URI, "name");
    private static final Property PROP_EMAIL         = ResourceFactory.createProperty(BASE_URI, "email");

    // ─── Propiedades Venta ────────────────────────────────────────────────────
    private static final Property PROP_TOTAL_VENTA   = ResourceFactory.createProperty(BASE_URI, "totalVenta");
    private static final Property PROP_TIENE_CLIENTE = ResourceFactory.createProperty(BASE_URI, "tieneCliente");
    private static final Property PROP_TIENE_PAGO    = ResourceFactory.createProperty(BASE_URI, "tienePago");
    private static final Property PROP_TIENE_DETALLE = ResourceFactory.createProperty(BASE_URI, "tieneDetalle");

    // ─── Propiedades Pago ─────────────────────────────────────────────────────
    private static final Property PROP_MONTO_PAGO    = ResourceFactory.createProperty(BASE_URI, "montoPago");
    private static final Property PROP_ESTADO_PAGO   = ResourceFactory.createProperty(BASE_URI, "estadoPago");
    private static final Property PROP_TIPO_COMP     = ResourceFactory.createProperty(BASE_URI, "tipoComprobante");
    private static final Property PROP_FECHA_PAGO    = ResourceFactory.createProperty(BASE_URI, "fechaPago");
    private static final Property PROP_METODO_PAGO   = ResourceFactory.createProperty(BASE_URI, "tieneMetodoPago");

    // ─── Propiedades DetalleVenta ─────────────────────────────────────────────
    private static final Property PROP_CANTIDAD_DET  = ResourceFactory.createProperty(BASE_URI, "cantidadDetalle");
    private static final Property PROP_PRECIO_UNIT   = ResourceFactory.createProperty(BASE_URI, "precioUnitario");
    private static final Property PROP_SUBTOTAL      = ResourceFactory.createProperty(BASE_URI, "subtotal");
    private static final Property PROP_DET_REPUESTO  = ResourceFactory.createProperty(BASE_URI, "detalleRepuesto");

    // ─── Propiedades Movimiento ───────────────────────────────────────────────
    private static final Property PROP_CANT_MOV      = ResourceFactory.createProperty(BASE_URI, "cantidadMovimiento");
    private static final Property PROP_DIA_MOV       = ResourceFactory.createProperty(BASE_URI, "diaMovimiento");
    private static final Property PROP_MES_MOV       = ResourceFactory.createProperty(BASE_URI, "mesMovimiento");
    private static final Property PROP_ANIO_MOV      = ResourceFactory.createProperty(BASE_URI, "anioMovimiento");
    private static final Property PROP_MOV_REPUESTO  = ResourceFactory.createProperty(BASE_URI, "movimientoRepuesto");
    private static final Property PROP_TIPO_MOV      = ResourceFactory.createProperty(BASE_URI, "tieneTipoMovimiento");

    // ─── Repositorios JPA ─────────────────────────────────────────────────────
    private final Dataset              dataset;
    private final RepuestoRepository   repuestoRepository;
    private final ClienteRepository    clienteRepository;
    private final VentaRepository      ventaRepository;
    private final PagoRepository       pagoRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final MovimientoRepository movimientoRepository;

    public OntologyLoaderService(Dataset dataset,
                                 RepuestoRepository repuestoRepository,
                                 ClienteRepository clienteRepository,
                                 VentaRepository ventaRepository,
                                 PagoRepository pagoRepository,
                                 DetalleVentaRepository detalleVentaRepository,
                                 MovimientoRepository movimientoRepository) {
        this.dataset                = dataset;
        this.repuestoRepository     = repuestoRepository;
        this.clienteRepository      = clienteRepository;
        this.ventaRepository        = ventaRepository;
        this.pagoRepository         = pagoRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.movimientoRepository   = movimientoRepository;
    }

    // ─── Arranque ─────────────────────────────────────────────────────────────

    /**
     * Se ejecuta automáticamente al arrancar Spring Boot.
     * Carga todas las entidades de la BD en el grafo RDF.
     */
    @PostConstruct
    public void cargarDesdeBD() {
        log.info("[RDF] Iniciando carga dinámica desde la base de datos...");
        limpiarInstancias();
        poblarTodo();
        log.info("[RDF] Carga finalizada.");
    }

    /**
     * Método público para recargar el grafo RDF desde la BD.
     * Llamar después de operaciones CRUD para mantener el grafo sincronizado.
     */
    public void recargar() {
        log.info("[RDF] Recarga manual solicitada...");
        limpiarInstancias();
        poblarTodo();
        log.info("[RDF] Recarga finalizada.");
    }

    // ─── Limpieza ─────────────────────────────────────────────────────────────

    /**
     * Elimina del grafo todas las instancias de las 6 clases del dominio
     * para evitar duplicados antes de una recarga.
     */
    private void limpiarInstancias() {
        String[] clases = {
            BASE_URI + "Repuesto",
            BASE_URI + "Cliente",
            BASE_URI + "Venta",
            BASE_URI + "Pago",
            BASE_URI + "DetalleVenta",
            BASE_URI + "Movimiento"
        };

        for (String clase : clases) {
            String sparqlDelete =
                "PREFIX vr: <" + BASE_URI + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "DELETE { ?r ?p ?o } " +
                "WHERE  { ?r rdf:type <" + clase + "> . ?r ?p ?o . }";

            Txn.executeWrite(dataset, () ->
                UpdateExecutionFactory
                    .create(UpdateFactory.create(sparqlDelete), dataset)
                    .execute()
            );
        }
        log.info("[RDF] Instancias anteriores eliminadas del grafo.");
    }

    // ─── Población ────────────────────────────────────────────────────────────

    private void poblarTodo() {
        poblarRepuestos();
        poblarClientes();
        poblarVentas();       // incluye DetalleVenta
        poblarPagos();
        poblarMovimientos();
    }

    // ── Repuestos ──────────────────────────────────────────────────────────

    private void poblarRepuestos() {
        List<Repuesto> lista = repuestoRepository.findAll();
        log.info("[RDF] Insertando {} repuesto(s)...", lista.size());

        Txn.executeWrite(dataset, () -> {
            Model model = dataset.getDefaultModel();
            for (Repuesto r : lista) {
                Resource res = model.createResource(BASE_URI + "repuesto_" + r.getId());
                res.addProperty(RDF.type, CLASS_REPUESTO);

                if (r.getNombre() != null)
                    res.addProperty(PROP_NAME, r.getNombre());
                if (r.getDescripcion() != null)
                    res.addProperty(PROP_DESCRIPCION, r.getDescripcion());
                if (r.getPrecio() != null)
                    res.addProperty(PROP_PRECIO, model.createTypedLiteral(r.getPrecio()));
                if (r.getStock() != null)
                    res.addProperty(PROP_STOCK, model.createTypedLiteral(r.getStock()));
                if (r.getMarca() != null) {
                    String marcaUri = resolverMarcaUri(r.getMarca().name());
                    if (marcaUri != null)
                        res.addProperty(PROP_TIENE_MARCA, model.createResource(marcaUri));
                }
            }
        });
        log.info("[RDF] {} repuesto(s) sincronizados.", lista.size());
    }

    // ── Clientes ───────────────────────────────────────────────────────────

    private void poblarClientes() {
        List<Cliente> lista = clienteRepository.findAll();
        log.info("[RDF] Insertando {} cliente(s)...", lista.size());

        Txn.executeWrite(dataset, () -> {
            Model model = dataset.getDefaultModel();
            for (Cliente c : lista) {
                Resource res = model.createResource(BASE_URI + "cliente_" + c.getId());
                res.addProperty(RDF.type, CLASS_CLIENTE);

                if (c.getNombre() != null) {
                    res.addProperty(PROP_FOAF_NAME, c.getNombre());
                    res.addProperty(PROP_NAME, c.getNombre());
                }
                if (c.getEmail() != null)
                    res.addProperty(PROP_EMAIL, c.getEmail());
            }
        });
        log.info("[RDF] {} cliente(s) sincronizados.", lista.size());
    }

    // ── Ventas + DetalleVenta ──────────────────────────────────────────────

    private void poblarVentas() {
        List<Venta> lista = ventaRepository.findAll();
        log.info("[RDF] Insertando {} venta(s) con sus detalles...", lista.size());

        Txn.executeWrite(dataset, () -> {
            Model model = dataset.getDefaultModel();
            for (Venta v : lista) {
                String ventaUri = BASE_URI + "venta_" + v.getId();
                Resource ventaRes = model.createResource(ventaUri);
                ventaRes.addProperty(RDF.type, CLASS_VENTA);

                if (v.getTotal() != null)
                    ventaRes.addProperty(PROP_TOTAL_VENTA, model.createTypedLiteral(v.getTotal()));

                // Relación → Cliente
                if (v.getCliente() != null) {
                    Resource clienteRes = model.createResource(BASE_URI + "cliente_" + v.getCliente().getId());
                    ventaRes.addProperty(PROP_TIENE_CLIENTE, clienteRes);
                }

                // Relación → Pago
                if (v.getPago() != null) {
                    Resource pagoRes = model.createResource(BASE_URI + "pago_" + v.getPago().getId());
                    ventaRes.addProperty(PROP_TIENE_PAGO, pagoRes);
                }

                // Detalles de la venta
                if (v.getDetalles() != null) {
                    for (DetalleVenta d : v.getDetalles()) {
                        String detalleUri = BASE_URI + "detalle_" + d.getId();
                        Resource detRes = model.createResource(detalleUri);
                        detRes.addProperty(RDF.type, CLASS_DETALLE);
                        // cantidad es int primitivo, siempre se puede insertar
                        detRes.addProperty(PROP_CANTIDAD_DET, model.createTypedLiteral(d.getCantidad()));

                        if (d.getPrecioUnitario() != null)
                            detRes.addProperty(PROP_PRECIO_UNIT, model.createTypedLiteral(d.getPrecioUnitario()));
                        if (d.getSubtotal() != null)
                            detRes.addProperty(PROP_SUBTOTAL, model.createTypedLiteral(d.getSubtotal()));

                        // Relación detalle → repuesto
                        if (d.getRepuesto() != null) {
                            Resource repRes = model.createResource(BASE_URI + "repuesto_" + d.getRepuesto().getId());
                            detRes.addProperty(PROP_DET_REPUESTO, repRes);
                        }

                        // Relación venta → detalle
                        ventaRes.addProperty(PROP_TIENE_DETALLE, detRes);
                    }
                }
            }
        });
        log.info("[RDF] {} venta(s) sincronizadas.", lista.size());
    }

    // ── Pagos ──────────────────────────────────────────────────────────────

    private void poblarPagos() {
        List<Pago> lista = pagoRepository.findAll();
        log.info("[RDF] Insertando {} pago(s)...", lista.size());

        Txn.executeWrite(dataset, () -> {
            Model model = dataset.getDefaultModel();
            for (Pago p : lista) {
                Resource res = model.createResource(BASE_URI + "pago_" + p.getId());
                res.addProperty(RDF.type, CLASS_PAGO);
                res.addProperty(PROP_MONTO_PAGO, model.createTypedLiteral(p.getMonto()));

                if (p.getEstadoPago() != null)
                    res.addProperty(PROP_ESTADO_PAGO, p.getEstadoPago().name());
                if (p.getTipoComprobante() != null)
                    res.addProperty(PROP_TIPO_COMP, p.getTipoComprobante().name());
                if (p.getFecha() != null)
                    res.addProperty(PROP_FECHA_PAGO, p.getFecha().toString());
                if (p.getMetodoPago() != null) {
                    String metodoUri = resolverMetodoPagoUri(p.getMetodoPago().name());
                    if (metodoUri != null)
                        res.addProperty(PROP_METODO_PAGO, model.createResource(metodoUri));
                }
            }
        });
        log.info("[RDF] {} pago(s) sincronizados.", lista.size());
    }

    // ── Movimientos ────────────────────────────────────────────────────────

    private void poblarMovimientos() {
        List<Movimiento> lista = movimientoRepository.findAll();
        log.info("[RDF] Insertando {} movimiento(s)...", lista.size());

        Txn.executeWrite(dataset, () -> {
            Model model = dataset.getDefaultModel();
            for (Movimiento m : lista) {
                Resource res = model.createResource(BASE_URI + "movimiento_" + m.getIdMovimiento());
                res.addProperty(RDF.type, CLASS_MOVIMIENTO);

                if (m.getCantidadDeMovimientos() != null)
                    res.addProperty(PROP_CANT_MOV, model.createTypedLiteral(m.getCantidadDeMovimientos()));

                // Fecha embebida
                if (m.getFechaMovimiento() != null) {
                    FechaMovimiento f = m.getFechaMovimiento();
                    if (f.getDia()  != null) res.addProperty(PROP_DIA_MOV,  model.createTypedLiteral(f.getDia()));
                    if (f.getMes()  != null) res.addProperty(PROP_MES_MOV,  model.createTypedLiteral(f.getMes()));
                    if (f.getAnio() != null) res.addProperty(PROP_ANIO_MOV, model.createTypedLiteral(f.getAnio()));
                }

                // Relación → tipo de movimiento (SKOS)
                if (m.getTipoMovimiento() != null) {
                    String tipoUri = resolverTipoMovimientoUri(m.getTipoMovimiento().name());
                    if (tipoUri != null)
                        res.addProperty(PROP_TIPO_MOV, model.createResource(tipoUri));
                }

                // Relación → repuesto
                if (m.getRepuesto() != null) {
                    Resource repRes = model.createResource(BASE_URI + "repuesto_" + m.getRepuesto().getId());
                    res.addProperty(PROP_MOV_REPUESTO, repRes);
                }
            }
        });
        log.info("[RDF] {} movimiento(s) sincronizados.", lista.size());
    }

    // ─── Resolvers SKOS ───────────────────────────────────────────────────────

    private String resolverMarcaUri(String marca) {
        return switch (marca.toUpperCase()) {
            case "BOSCH"   -> BASE_URI + "MarcaBosch";
            case "STANLEY" -> BASE_URI + "MarcaStanley";
            case "TRUPPER" -> BASE_URI + "MarcaTrupper";
            default -> { log.warn("[RDF] Marca desconocida: {}", marca); yield null; }
        };
    }

    private String resolverMetodoPagoUri(String metodo) {
        return switch (metodo.toUpperCase()) {
            case "EFECTIVO"         -> BASE_URI + "PagoEfectivo";
            case "TARJETA_CREDITO"  -> BASE_URI + "PagoTarjetaCredito";
            case "TARJETA_DEBITO"   -> BASE_URI + "PagoTarjetaDebito";
            case "TRANSFERENCIA"    -> BASE_URI + "PagoTransferencia";
            case "YAPE"             -> BASE_URI + "PagoYape";
            case "PLIN"             -> BASE_URI + "PagoPlin";
            default -> { log.warn("[RDF] Método de pago desconocido: {}", metodo); yield null; }
        };
    }

    private String resolverTipoMovimientoUri(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "VENTA"      -> BASE_URI + "MovimientoVenta";
            case "DEVOLUCION" -> BASE_URI + "MovimientoDevolucion";
            case "ENTRADA"    -> BASE_URI + "MovimientoEntrada";
            default -> { log.warn("[RDF] Tipo de movimiento desconocido: {}", tipo); yield null; }
        };
    }
}

