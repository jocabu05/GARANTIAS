package com.garantias.service;

import com.garantias.config.MongoDBConfig;
import com.garantias.model.Factura;
import com.garantias.model.Factura.EstadoFactura;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Servicio para gestión de Facturas en MongoDB
 */
public class FacturaService {
    
    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);
    private static final String COLLECTION_NAME = "facturas";
    
    private MongoCollection<Document> collection;
    
    public FacturaService() {
        this.collection = MongoDBConfig.getDatabase().getCollection(COLLECTION_NAME);
    }
    
    /**
     * Obtiene todas las facturas
     */
    public List<Factura> findAll() {
        List<Factura> facturas = new ArrayList<>();
        try {
            logger.info("Cargando facturas desde MongoDB...");
            for (Document doc : collection.find().sort(Sorts.descending("fechaEmision"))) {
                Factura f = documentToFactura(doc);
                if (f != null) {
                    facturas.add(f);
                }
            }
            logger.info("Facturas cargadas: {}", facturas.size());
        } catch (Exception e) {
            logger.error("Error en findAll: {}", e.getMessage(), e);
        }
        return facturas;
    }
    
    /**
     * Busca factura por ID
     */
    public Factura findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc != null ? documentToFactura(doc) : null;
    }
    
    /**
     * Busca factura por número
     */
    public Factura findByNumero(String numeroFactura) {
        Document doc = collection.find(Filters.eq("numeroFactura", numeroFactura)).first();
        return doc != null ? documentToFactura(doc) : null;
    }
    
    /**
     * Busca facturas por garantía
     */
    public Factura findByGarantiaId(ObjectId garantiaId) {
        Document doc = collection.find(Filters.eq("garantiaId", garantiaId)).first();
        return doc != null ? documentToFactura(doc) : null;
    }
    
    /**
     * Busca facturas por estado
     */
    public List<Factura> findByEstado(EstadoFactura estado) {
        List<Factura> facturas = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("estado", estado.name()))) {
            facturas.add(documentToFactura(doc));
        }
        return facturas;
    }
    
    /**
     * Busca facturas por rango de fechas
     */
    public List<Factura> findByFechaRange(LocalDate desde, LocalDate hasta) {
        List<Factura> facturas = new ArrayList<>();
        for (Document doc : collection.find(Filters.and(
            Filters.gte("fechaEmision", java.sql.Date.valueOf(desde)),
            Filters.lte("fechaEmision", java.sql.Date.valueOf(hasta))
        )).sort(Sorts.descending("fechaEmision"))) {
            facturas.add(documentToFactura(doc));
        }
        return facturas;
    }
    
    /**
     * Búsqueda por texto
     */
    public List<Factura> search(String texto) {
        List<Factura> facturas = new ArrayList<>();
        String regex = ".*" + texto + ".*";
        
        for (Document doc : collection.find(Filters.or(
            Filters.regex("numeroFactura", regex, "i"),
            Filters.regex("cliente.nombre", regex, "i"),
            Filters.regex("cliente.nif", regex, "i")
        ))) {
            facturas.add(documentToFactura(doc));
        }
        return facturas;
    }
    
    /**
     * Inserta una nueva factura
     */
    public ObjectId insert(Factura factura) {
        Document doc = facturaToDocument(factura);
        InsertOneResult result = collection.insertOne(doc);
        logger.info("Factura insertada: {}", result.getInsertedId());
        return result.getInsertedId().asObjectId().getValue();
    }
    
    /**
     * Actualiza una factura existente
     */
    public boolean update(Factura factura) {
        factura.setFechaActualizacion(LocalDateTime.now());
        Document doc = facturaToDocument(factura);
        doc.remove("_id");
        
        UpdateResult result = collection.replaceOne(
            Filters.eq("_id", factura.getId()),
            doc
        );
        
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Elimina una factura
     */
    public boolean delete(ObjectId id) {
        DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Genera el próximo número de factura
     */
    public String generateNextNumero() {
        int year = LocalDate.now().getYear();
        String prefix = "FAC-" + year + "-";
        
        Document lastDoc = collection.find(Filters.regex("numeroFactura", "^" + prefix))
            .sort(Sorts.descending("numeroFactura"))
            .first();
        
        int nextNumber = 1;
        if (lastDoc != null) {
            String lastNumero = lastDoc.getString("numeroFactura");
            String[] parts = lastNumero.split("-");
            nextNumber = Integer.parseInt(parts[2]) + 1;
        }
        
        return String.format("%s%04d", prefix, nextNumber);
    }
    
    /**
     * Obtiene totales por estado
     */
    public Map<EstadoFactura, Double> getTotalesByEstado() {
        Map<EstadoFactura, Double> totales = new HashMap<>();
        
        for (EstadoFactura estado : EstadoFactura.values()) {
            double total = 0;
            for (Document doc : collection.find(Filters.eq("estado", estado.name()))) {
                Object docTotal = doc.get("total");
                if (docTotal instanceof Number) {
                    total += ((Number) docTotal).doubleValue();
                }
            }
            totales.put(estado, total);
        }
        
        return totales;
    }
    
    /**
     * Obtiene facturación por mes del año actual
     */
    public Map<Integer, Double> getFacturacionPorMes() {
        Map<Integer, Double> facturacion = new LinkedHashMap<>();
        
        // Inicializar todos los meses
        for (int i = 1; i <= 12; i++) {
            facturacion.put(i, 0.0);
        }
        
        int year = LocalDate.now().getYear();
        LocalDate inicioAno = LocalDate.of(year, 1, 1);
        LocalDate finAno = LocalDate.of(year, 12, 31);
        
        for (Document doc : collection.find(Filters.and(
            Filters.eq("estado", "PAGADA"),
            Filters.gte("fechaEmision", java.sql.Date.valueOf(inicioAno)),
            Filters.lte("fechaEmision", java.sql.Date.valueOf(finAno))
        ))) {
            Date fecha = doc.getDate("fechaEmision");
            if (fecha != null) {
                int mes = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue();
                Object total = doc.get("total");
                if (total instanceof Number) {
                    facturacion.put(mes, facturacion.get(mes) + ((Number) total).doubleValue());
                }
            }
        }
        
        return facturacion;
    }
    
    /**
     * Cuenta total de facturas
     */
    public long countTotal() {
        return collection.countDocuments();
    }
    
    /**
     * Suma total facturado (facturas pagadas)
     */
    public double getTotalFacturado() {
        double total = 0;
        for (Document doc : collection.find(Filters.eq("estado", "PAGADA"))) {
            Object docTotal = doc.get("total");
            if (docTotal instanceof Number) {
                total += ((Number) docTotal).doubleValue();
            }
        }
        return total;
    }
    
    // Conversión Document -> Factura
    private Factura documentToFactura(Document doc) {
        try {
            Factura f = new Factura();
            f.setId(doc.getObjectId("_id"));
            f.setNumeroFactura(doc.getString("numeroFactura"));
            f.setGarantiaId(doc.getObjectId("garantiaId"));
            f.setNotas(doc.getString("notas"));
            
            // Subtotales y totales - usar Number para manejar Integer y Double
            Number subtotal = (Number) doc.get("subtotal");
            Number totalIVA = (Number) doc.get("totalIVA");
            Number total = (Number) doc.get("total");
            f.setSubtotal(subtotal != null ? subtotal.doubleValue() : 0.0);
            f.setTotalIVA(totalIVA != null ? totalIVA.doubleValue() : 0.0);
            f.setTotal(total != null ? total.doubleValue() : 0.0);
            
            // Estado y método de pago
            String estadoStr = doc.getString("estadoFactura");
            if (estadoStr == null) estadoStr = doc.getString("estado");
            if (estadoStr != null) {
                try {
                    f.setEstado(EstadoFactura.valueOf(estadoStr));
                } catch (IllegalArgumentException e) {
                    logger.warn("Estado no reconocido: {}", estadoStr);
                }
            }
            String metodoStr = doc.getString("metodoPago");
            if (metodoStr != null) {
                try {
                    f.setMetodoPago(Factura.MetodoPago.valueOf(metodoStr));
                } catch (IllegalArgumentException e) {
                    logger.warn("Método de pago no reconocido: {}", metodoStr);
                }
            }
            
            // Cliente
            Document clienteDoc = doc.get("cliente", Document.class);
            if (clienteDoc != null) {
                Factura.DatosCliente cliente = new Factura.DatosCliente(
                    clienteDoc.getString("nombre"),
                    clienteDoc.getString("nif"),
                    clienteDoc.getString("direccion")
                );
                f.setCliente(cliente);
            }
            
            // Items
            List<Document> itemDocs = doc.getList("items", Document.class);
            if (itemDocs != null) {
                List<Factura.ItemFactura> items = new ArrayList<>();
                for (Document itemDoc : itemDocs) {
                    Factura.ItemFactura item = new Factura.ItemFactura();
                    item.setDescripcion(itemDoc.getString("descripcion"));
                    
                    // Manejar cantidad como Number
                    Number cantidad = (Number) itemDoc.get("cantidad");
                    item.setCantidad(cantidad != null ? cantidad.intValue() : 0);
                    
                    // Manejar precioUnitario como Number
                    Number precioUnit = (Number) itemDoc.get("precioUnitario");
                    item.setPrecioUnitario(precioUnit != null ? precioUnit.doubleValue() : 0.0);
                    
                    // Manejar IVA como Number
                    Number iva = (Number) itemDoc.get("iva");
                    item.setIva(iva != null ? iva.intValue() : 21);
                    
                    // Manejar total como Number
                    Number totalItem = (Number) itemDoc.get("total");
                    item.setTotal(totalItem != null ? totalItem.doubleValue() : 0.0);
                    
                    items.add(item);
                }
                f.setItems(items);
            }
            
            // Fechas
            Date fechaEmision = doc.getDate("fechaEmision");
            if (fechaEmision != null) {
                f.setFechaEmision(fechaEmision.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            
            logger.debug("Factura convertida: {}", f.getNumeroFactura());
            return f;
        } catch (Exception e) {
            logger.error("Error convirtiendo documento a factura: {}", e.getMessage(), e);
            return null;
        }
    }
    
    // Conversión Factura -> Document
    private Document facturaToDocument(Factura f) {
        Document doc = new Document();
        
        if (f.getId() != null) {
            doc.append("_id", f.getId());
        }
        doc.append("numeroFactura", f.getNumeroFactura());
        if (f.getGarantiaId() != null) {
            doc.append("garantiaId", f.getGarantiaId());
        }
        doc.append("subtotal", f.getSubtotal());
        doc.append("totalIVA", f.getTotalIVA());
        doc.append("total", f.getTotal());
        doc.append("estado", f.getEstado() != null ? f.getEstado().name() : null);
        doc.append("metodoPago", f.getMetodoPago() != null ? f.getMetodoPago().name() : null);
        doc.append("notas", f.getNotas());
        
        // Cliente
        if (f.getCliente() != null) {
            doc.append("cliente", new Document()
                .append("nombre", f.getCliente().getNombre())
                .append("nif", f.getCliente().getNif())
                .append("direccion", f.getCliente().getDireccion())
            );
        }
        
        // Items
        if (f.getItems() != null) {
            List<Document> itemDocs = new ArrayList<>();
            for (Factura.ItemFactura item : f.getItems()) {
                itemDocs.add(new Document()
                    .append("descripcion", item.getDescripcion())
                    .append("cantidad", item.getCantidad())
                    .append("precioUnitario", item.getPrecioUnitario())
                    .append("iva", item.getIva())
                    .append("total", item.getTotal())
                );
            }
            doc.append("items", itemDocs);
        }
        
        // Fechas
        if (f.getFechaEmision() != null) {
            doc.append("fechaEmision", java.sql.Date.valueOf(f.getFechaEmision()));
        }
        if (f.getFechaCreacion() != null) {
            doc.append("fechaCreacion", Date.from(f.getFechaCreacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (f.getFechaActualizacion() != null) {
            doc.append("fechaActualizacion", Date.from(f.getFechaActualizacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
}
