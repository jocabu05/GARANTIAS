package com.garantias.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de datos para Factura
 */
public class Factura {
    
    @BsonId
    private ObjectId id;
    
    @BsonProperty("numeroFactura")
    private String numeroFactura;
    
    private ObjectId garantiaId;
    private DatosCliente cliente;
    private LocalDate fechaEmision;
    private List<ItemFactura> items;
    private Double subtotal;
    private Double totalIVA;
    private Double total;
    private EstadoFactura estado;
    private MetodoPago metodoPago;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío
    public Factura() {
        this.items = new ArrayList<>();
        this.fechaEmision = LocalDate.now();
        this.estado = EstadoFactura.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Constructor con datos básicos
    public Factura(String numeroFactura, DatosCliente cliente) {
        this();
        this.numeroFactura = numeroFactura;
        this.cliente = cliente;
    }
    
    // Clase interna: DatosCliente
    public static class DatosCliente {
        private String nombre;
        private String nif;
        private String direccion;
        
        public DatosCliente() {}
        
        public DatosCliente(String nombre, String nif, String direccion) {
            this.nombre = nombre;
            this.nif = nif;
            this.direccion = direccion;
        }
        
        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getNif() { return nif; }
        public void setNif(String nif) { this.nif = nif; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }
    
    // Clase interna: ItemFactura
    public static class ItemFactura {
        private String descripcion;
        private Integer cantidad;
        private Double precioUnitario;
        private Integer iva;
        private Double total;
        
        public ItemFactura() {}
        
        public ItemFactura(String descripcion, Integer cantidad, Double precioUnitario, Integer iva) {
            this.descripcion = descripcion;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.iva = iva;
            calcularTotal();
        }
        
        public void calcularTotal() {
            double baseImponible = cantidad * precioUnitario;
            double importeIVA = baseImponible * (iva / 100.0);
            this.total = baseImponible + importeIVA;
        }
        
        // Getters y Setters
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public Double getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
        public Integer getIva() { return iva; }
        public void setIva(Integer iva) { this.iva = iva; }
        public Double getTotal() { return total; }
        public void setTotal(Double total) { this.total = total; }
        
        public Double getBaseImponible() {
            return cantidad * precioUnitario;
        }
        
        public Double getImporteIVA() {
            return getBaseImponible() * (iva / 100.0);
        }
    }
    
    // Enums
    public enum EstadoFactura {
        PENDIENTE("Pendiente", "#FF9800"),
        PAGADA("Pagada", "#4CAF50"),
        ANULADA("Anulada", "#F44336");
        
        private final String displayName;
        private final String color;
        
        EstadoFactura(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA("Tarjeta"),
        TRANSFERENCIA("Transferencia"),
        BIZUM("Bizum"),
        FINANCIADO("Financiado");
        
        private final String displayName;
        
        MetodoPago(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    // Métodos de cálculo
    public void recalcularTotales() {
        this.subtotal = 0.0;
        this.totalIVA = 0.0;
        
        for (ItemFactura item : items) {
            item.calcularTotal();
            this.subtotal += item.getBaseImponible();
            this.totalIVA += item.getImporteIVA();
        }
        
        this.total = this.subtotal + this.totalIVA;
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public void addItem(ItemFactura item) {
        this.items.add(item);
        recalcularTotales();
    }
    
    public void removeItem(ItemFactura item) {
        this.items.remove(item);
        recalcularTotales();
    }
    
    // Getters y Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
    public ObjectId getGarantiaId() { return garantiaId; }
    public void setGarantiaId(ObjectId garantiaId) { this.garantiaId = garantiaId; }
    public DatosCliente getCliente() { return cliente; }
    public void setCliente(DatosCliente cliente) { this.cliente = cliente; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }
    public List<ItemFactura> getItems() { return items; }
    public void setItems(List<ItemFactura> items) { this.items = items; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Double getTotalIVA() { return totalIVA; }
    public void setTotalIVA(Double totalIVA) { this.totalIVA = totalIVA; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
