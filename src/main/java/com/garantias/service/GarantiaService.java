package com.garantias.service;

import com.garantias.config.MongoDBConfig;
import com.garantias.model.Garantia;
import com.garantias.model.Garantia.EstadoGarantia;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Servicio para gestión de Garantías en MongoDB
 */
public class GarantiaService {
    
    private static final Logger logger = LoggerFactory.getLogger(GarantiaService.class);
    private static final String COLLECTION_NAME = "garantias";
    
    private MongoCollection<Document> collection;
    
    public GarantiaService() {
        this.collection = MongoDBConfig.getDatabase().getCollection(COLLECTION_NAME);
    }
    
    /**
     * Obtiene todas las garantías
     */
    public List<Garantia> findAll() {
        List<Garantia> garantias = new ArrayList<>();
        for (Document doc : collection.find().sort(Sorts.descending("fechaCreacion"))) {
            garantias.add(documentToGarantia(doc));
        }
        return garantias;
    }
    
    /**
     * Busca garantía por ID
     */
    public Garantia findById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return doc != null ? documentToGarantia(doc) : null;
    }
    
    /**
     * Busca garantía por número
     */
    public Garantia findByNumero(String numeroGarantia) {
        Document doc = collection.find(Filters.eq("numeroGarantia", numeroGarantia)).first();
        return doc != null ? documentToGarantia(doc) : null;
    }
    
    /**
     * Busca garantías por estado
     */
    public List<Garantia> findByEstado(EstadoGarantia estado) {
        List<Garantia> garantias = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("garantia.estado", estado.name()))) {
            garantias.add(documentToGarantia(doc));
        }
        return garantias;
    }
    
    /**
     * Busca garantías próximas a vencer (30 días por defecto)
     */
    public List<Garantia> findProximasAVencer(int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        
        List<Garantia> garantias = new ArrayList<>();
        Bson filter = Filters.and(
            Filters.eq("garantia.estado", "ACTIVA"),
            Filters.gte("garantia.fechaFin", java.sql.Date.valueOf(hoy)),
            Filters.lte("garantia.fechaFin", java.sql.Date.valueOf(limite))
        );
        
        for (Document doc : collection.find(filter).sort(Sorts.ascending("garantia.fechaFin"))) {
            garantias.add(documentToGarantia(doc));
        }
        return garantias;
    }
    
    /**
     * Búsqueda por texto (cliente, número serie, etc.)
     */
    public List<Garantia> search(String texto) {
        List<Garantia> garantias = new ArrayList<>();
        String regex = ".*" + texto + ".*";
        
        Bson filter = Filters.or(
            Filters.regex("numeroGarantia", regex, "i"),
            Filters.regex("cliente.nombre", regex, "i"),
            Filters.regex("cliente.telefono", regex, "i"),
            Filters.regex("aireAcondicionado.numeroSerie", regex, "i"),
            Filters.regex("aireAcondicionado.marca", regex, "i")
        );
        
        for (Document doc : collection.find(filter)) {
            garantias.add(documentToGarantia(doc));
        }
        return garantias;
    }
    
    /**
     * Inserta una nueva garantía
     */
    public ObjectId insert(Garantia garantia) {
        Document doc = garantiaToDocument(garantia);
        InsertOneResult result = collection.insertOne(doc);
        logger.info("Garantía insertada: {}", result.getInsertedId());
        return result.getInsertedId().asObjectId().getValue();
    }
    
    /**
     * Actualiza una garantía existente
     */
    public boolean update(Garantia garantia) {
        garantia.setFechaActualizacion(LocalDateTime.now());
        Document doc = garantiaToDocument(garantia);
        doc.remove("_id");
        
        UpdateResult result = collection.replaceOne(
            Filters.eq("_id", garantia.getId()),
            doc
        );
        
        logger.info("Garantía actualizada: {} modificados", result.getModifiedCount());
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Cambia el estado de una garantía
     */
    public boolean updateEstado(ObjectId id, EstadoGarantia nuevoEstado) {
        UpdateResult result = collection.updateOne(
            Filters.eq("_id", id),
            Updates.combine(
                Updates.set("garantia.estado", nuevoEstado.name()),
                Updates.set("fechaActualizacion", new Date())
            )
        );
        return result.getModifiedCount() > 0;
    }
    
    /**
     * Elimina una garantía
     */
    public boolean delete(ObjectId id) {
        DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
        logger.info("Garantía eliminada: {}", result.getDeletedCount() > 0);
        return result.getDeletedCount() > 0;
    }
    
    /**
     * Genera el próximo número de garantía
     */
    public String generateNextNumero() {
        int year = LocalDate.now().getYear();
        String prefix = "GAR-" + year + "-";
        
        Document lastDoc = collection.find(Filters.regex("numeroGarantia", "^" + prefix))
            .sort(Sorts.descending("numeroGarantia"))
            .first();
        
        int nextNumber = 1;
        if (lastDoc != null) {
            String lastNumero = lastDoc.getString("numeroGarantia");
            String[] parts = lastNumero.split("-");
            nextNumber = Integer.parseInt(parts[2]) + 1;
        }
        
        return String.format("%s%04d", prefix, nextNumber);
    }
    
    /**
     * Cuenta garantías por estado
     */
    public Map<EstadoGarantia, Long> countByEstado() {
        Map<EstadoGarantia, Long> counts = new HashMap<>();
        for (EstadoGarantia estado : EstadoGarantia.values()) {
            long count = collection.countDocuments(Filters.eq("garantia.estado", estado.name()));
            counts.put(estado, count);
        }
        return counts;
    }
    
    /**
     * Cuenta garantías por marca
     */
    public Map<String, Long> countByMarca() {
        Map<String, Long> counts = new HashMap<>();
        List<Document> pipeline = Arrays.asList(
            new Document("$group", new Document("_id", "$aireAcondicionado.marca")
                .append("count", new Document("$sum", 1)))
        );
        
        for (Document doc : collection.aggregate(pipeline)) {
            String marca = doc.getString("_id");
            if (marca != null) {
                Number countNum = (Number) doc.get("count");
                counts.put(marca, countNum != null ? countNum.longValue() : 0L);
            }
        }
        return counts;
    }
    
    /**
     * Cuenta total de garantías
     */
    public long countTotal() {
        return collection.countDocuments();
    }
    
    // Conversión Document -> Garantia
    private Garantia documentToGarantia(Document doc) {
        Garantia g = new Garantia();
        g.setId(doc.getObjectId("_id"));
        g.setNumeroGarantia(doc.getString("numeroGarantia"));
        g.setNotas(doc.getString("notas"));
        g.setCreadoPor(doc.getString("creadoPor"));
        
        // Cliente
        Document clienteDoc = doc.get("cliente", Document.class);
        if (clienteDoc != null) {
            Garantia.Cliente cliente = new Garantia.Cliente(
                clienteDoc.getString("nombre"),
                clienteDoc.getString("telefono"),
                clienteDoc.getString("email"),
                clienteDoc.getString("direccion")
            );
            g.setCliente(cliente);
        }
        
        // Aire Acondicionado
        Document aireDoc = doc.get("aireAcondicionado", Document.class);
        if (aireDoc != null) {
            Garantia.AireAcondicionado aire = new Garantia.AireAcondicionado();
            aire.setMarca(aireDoc.getString("marca"));
            aire.setModelo(aireDoc.getString("modelo"));
            aire.setNumeroSerie(aireDoc.getString("numeroSerie"));
            aire.setTipoRefrigerante(aireDoc.getString("tipoRefrigerante"));
            aire.setPotenciaBTU(aireDoc.getInteger("potenciaBTU"));
            Date fechaInst = aireDoc.getDate("fechaInstalacion");
            if (fechaInst != null) {
                aire.setFechaInstalacion(fechaInst.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            g.setAireAcondicionado(aire);
        }
        
        // Detalles garantía
        Document garDoc = doc.get("garantia", Document.class);
        if (garDoc != null) {
            Garantia.DetalleGarantia detalle = new Garantia.DetalleGarantia();
            Date fechaInicio = garDoc.getDate("fechaInicio");
            Date fechaFin = garDoc.getDate("fechaFin");
            if (fechaInicio != null) {
                detalle.setFechaInicio(fechaInicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            if (fechaFin != null) {
                detalle.setFechaFin(fechaFin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            }
            detalle.setDuracionMeses(garDoc.getInteger("duracionMeses"));
            String tipoStr = garDoc.getString("tipo");
            if (tipoStr != null) {
                detalle.setTipo(Garantia.TipoGarantia.valueOf(tipoStr));
            }
            String estadoStr = garDoc.getString("estado");
            if (estadoStr != null) {
                detalle.setEstado(EstadoGarantia.valueOf(estadoStr));
            }
            detalle.setCobertura(garDoc.getList("cobertura", String.class));
            g.setGarantia(detalle);
        }
        
        // Fechas
        Date fechaCreacion = doc.getDate("fechaCreacion");
        if (fechaCreacion != null) {
            g.setFechaCreacion(fechaCreacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        Date fechaActualizacion = doc.getDate("fechaActualizacion");
        if (fechaActualizacion != null) {
            g.setFechaActualizacion(fechaActualizacion.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        
        return g;
    }
    
    // Conversión Garantia -> Document
    private Document garantiaToDocument(Garantia g) {
        Document doc = new Document();
        
        if (g.getId() != null) {
            doc.append("_id", g.getId());
        }
        doc.append("numeroGarantia", g.getNumeroGarantia());
        doc.append("notas", g.getNotas());
        doc.append("creadoPor", g.getCreadoPor());
        
        // Cliente
        if (g.getCliente() != null) {
            doc.append("cliente", new Document()
                .append("nombre", g.getCliente().getNombre())
                .append("telefono", g.getCliente().getTelefono())
                .append("email", g.getCliente().getEmail())
                .append("direccion", g.getCliente().getDireccion())
            );
        }
        
        // Aire Acondicionado
        if (g.getAireAcondicionado() != null) {
            Document aireDoc = new Document()
                .append("marca", g.getAireAcondicionado().getMarca())
                .append("modelo", g.getAireAcondicionado().getModelo())
                .append("numeroSerie", g.getAireAcondicionado().getNumeroSerie())
                .append("tipoRefrigerante", g.getAireAcondicionado().getTipoRefrigerante())
                .append("potenciaBTU", g.getAireAcondicionado().getPotenciaBTU());
            if (g.getAireAcondicionado().getFechaInstalacion() != null) {
                aireDoc.append("fechaInstalacion", java.sql.Date.valueOf(g.getAireAcondicionado().getFechaInstalacion()));
            }
            doc.append("aireAcondicionado", aireDoc);
        }
        
        // Detalles garantía
        if (g.getGarantia() != null) {
            Document garDoc = new Document()
                .append("duracionMeses", g.getGarantia().getDuracionMeses())
                .append("tipo", g.getGarantia().getTipo() != null ? g.getGarantia().getTipo().name() : null)
                .append("estado", g.getGarantia().getEstado() != null ? g.getGarantia().getEstado().name() : null)
                .append("cobertura", g.getGarantia().getCobertura());
            if (g.getGarantia().getFechaInicio() != null) {
                garDoc.append("fechaInicio", java.sql.Date.valueOf(g.getGarantia().getFechaInicio()));
            }
            if (g.getGarantia().getFechaFin() != null) {
                garDoc.append("fechaFin", java.sql.Date.valueOf(g.getGarantia().getFechaFin()));
            }
            doc.append("garantia", garDoc);
        }
        
        // Fechas
        if (g.getFechaCreacion() != null) {
            doc.append("fechaCreacion", Date.from(g.getFechaCreacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (g.getFechaActualizacion() != null) {
            doc.append("fechaActualizacion", Date.from(g.getFechaActualizacion().atZone(ZoneId.systemDefault()).toInstant()));
        }
        
        return doc;
    }
}
