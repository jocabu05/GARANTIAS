package com.garantias.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de datos para Garantía
 */
public class Garantia {
    
    @BsonId
    private ObjectId id;
    
    @BsonProperty("numeroGarantia")
    private String numeroGarantia;
    
    private Cliente cliente;
    private AireAcondicionado aireAcondicionado;
    private DetalleGarantia garantia;
    private List<Reparacion> historialReparaciones;
    private ObjectId facturaId;
    private String notas;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío requerido para BSON
    public Garantia() {
        this.historialReparaciones = new ArrayList<>();
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    // Constructor completo
    public Garantia(String numeroGarantia, Cliente cliente, AireAcondicionado aireAcondicionado, 
                    DetalleGarantia garantia, String creadoPor) {
        this();
        this.numeroGarantia = numeroGarantia;
        this.cliente = cliente;
        this.aireAcondicionado = aireAcondicionado;
        this.garantia = garantia;
        this.creadoPor = creadoPor;
    }
    
    // Clase interna: Cliente
    public static class Cliente {
        private String nombre;
        private String telefono;
        private String email;
        private String direccion;
        
        public Cliente() {}
        
        public Cliente(String nombre, String telefono, String email, String direccion) {
            this.nombre = nombre;
            this.telefono = telefono;
            this.email = email;
            this.direccion = direccion;
        }
        
        // Getters y Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }
    
    // Clase interna: AireAcondicionado
    public static class AireAcondicionado {
        private String marca;
        private String modelo;
        private String numeroSerie;
        private String tipoRefrigerante;
        private Integer potenciaBTU;
        private LocalDate fechaInstalacion;
        
        public AireAcondicionado() {}
        
        public AireAcondicionado(String marca, String modelo, String numeroSerie, 
                                  String tipoRefrigerante, Integer potenciaBTU, LocalDate fechaInstalacion) {
            this.marca = marca;
            this.modelo = modelo;
            this.numeroSerie = numeroSerie;
            this.tipoRefrigerante = tipoRefrigerante;
            this.potenciaBTU = potenciaBTU;
            this.fechaInstalacion = fechaInstalacion;
        }
        
        // Getters y Setters
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        public String getNumeroSerie() { return numeroSerie; }
        public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }
        public String getTipoRefrigerante() { return tipoRefrigerante; }
        public void setTipoRefrigerante(String tipoRefrigerante) { this.tipoRefrigerante = tipoRefrigerante; }
        public Integer getPotenciaBTU() { return potenciaBTU; }
        public void setPotenciaBTU(Integer potenciaBTU) { this.potenciaBTU = potenciaBTU; }
        public LocalDate getFechaInstalacion() { return fechaInstalacion; }
        public void setFechaInstalacion(LocalDate fechaInstalacion) { this.fechaInstalacion = fechaInstalacion; }
    }
    
    // Clase interna: DetalleGarantia
    public static class DetalleGarantia {
        private LocalDate fechaInicio;
        private LocalDate fechaFin;
        private Integer duracionMeses;
        private TipoGarantia tipo;
        private EstadoGarantia estado;
        private List<String> cobertura;
        
        public DetalleGarantia() {
            this.cobertura = new ArrayList<>();
        }
        
        public DetalleGarantia(LocalDate fechaInicio, Integer duracionMeses, 
                               TipoGarantia tipo, List<String> cobertura) {
            this.fechaInicio = fechaInicio;
            this.duracionMeses = duracionMeses;
            this.fechaFin = fechaInicio.plusMonths(duracionMeses);
            this.tipo = tipo;
            this.estado = EstadoGarantia.ACTIVA;
            this.cobertura = cobertura;
        }
        
        // Getters y Setters
        public LocalDate getFechaInicio() { return fechaInicio; }
        public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
        public LocalDate getFechaFin() { return fechaFin; }
        public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
        public Integer getDuracionMeses() { return duracionMeses; }
        public void setDuracionMeses(Integer duracionMeses) { this.duracionMeses = duracionMeses; }
        public TipoGarantia getTipo() { return tipo; }
        public void setTipo(TipoGarantia tipo) { this.tipo = tipo; }
        public EstadoGarantia getEstado() { return estado; }
        public void setEstado(EstadoGarantia estado) { this.estado = estado; }
        public List<String> getCobertura() { return cobertura; }
        public void setCobertura(List<String> cobertura) { this.cobertura = cobertura; }
    }
    
    // Clase interna: Reparacion
    public static class Reparacion {
        private LocalDate fecha;
        private String descripcion;
        private String tecnico;
        private Double costo;
        
        public Reparacion() {}
        
        public Reparacion(LocalDate fecha, String descripcion, String tecnico, Double costo) {
            this.fecha = fecha;
            this.descripcion = descripcion;
            this.tecnico = tecnico;
            this.costo = costo;
        }
        
        // Getters y Setters
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getTecnico() { return tecnico; }
        public void setTecnico(String tecnico) { this.tecnico = tecnico; }
        public Double getCosto() { return costo; }
        public void setCosto(Double costo) { this.costo = costo; }
    }
    
    // Enums
    public enum TipoGarantia {
        COMPLETA("Completa"),
        LIMITADA("Limitada"),
        EXTENDIDA("Extendida");
        
        private final String displayName;
        TipoGarantia(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum EstadoGarantia {
        ACTIVA("Activa", "#4CAF50"),
        VENCIDA("Vencida", "#9E9E9E"),
        RECLAMADA("Reclamada", "#FF9800"),
        ANULADA("Anulada", "#F44336");
        
        private final String displayName;
        private final String color;
        EstadoGarantia(String displayName, String color) { 
            this.displayName = displayName; 
            this.color = color;
        }
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    // Getters y Setters principales
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getNumeroGarantia() { return numeroGarantia; }
    public void setNumeroGarantia(String numeroGarantia) { this.numeroGarantia = numeroGarantia; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public AireAcondicionado getAireAcondicionado() { return aireAcondicionado; }
    public void setAireAcondicionado(AireAcondicionado aireAcondicionado) { this.aireAcondicionado = aireAcondicionado; }
    public DetalleGarantia getGarantia() { return garantia; }
    public void setGarantia(DetalleGarantia garantia) { this.garantia = garantia; }
    public List<Reparacion> getHistorialReparaciones() { return historialReparaciones; }
    public void setHistorialReparaciones(List<Reparacion> historialReparaciones) { this.historialReparaciones = historialReparaciones; }
    public ObjectId getFacturaId() { return facturaId; }
    public void setFacturaId(ObjectId facturaId) { this.facturaId = facturaId; }
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
    public String getCreadoPor() { return creadoPor; }
    public void setCreadoPor(String creadoPor) { this.creadoPor = creadoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    
    // Métodos de utilidad
    public void addReparacion(Reparacion reparacion) {
        this.historialReparaciones.add(reparacion);
        this.fechaActualizacion = LocalDateTime.now();
    }
    
    public boolean isProximaAVencer(int diasAnticipacion) {
        if (garantia == null || garantia.getFechaFin() == null) return false;
        LocalDate hoy = LocalDate.now();
        LocalDate fechaAlerta = garantia.getFechaFin().minusDays(diasAnticipacion);
        return hoy.isAfter(fechaAlerta) && hoy.isBefore(garantia.getFechaFin());
    }
    
    public long getDiasRestantes() {
        if (garantia == null || garantia.getFechaFin() == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), garantia.getFechaFin());
    }
}
