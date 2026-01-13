package com.garantias.util;

import com.garantias.model.Factura;
import com.garantias.model.Factura.*;
import com.garantias.service.FacturaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Utilidad para crear facturas de muestra
 */
public class FacturaSampleData {
    
    private static final Logger logger = LoggerFactory.getLogger(FacturaSampleData.class);
    
    public static void createSampleInvoices() {
        FacturaService service = new FacturaService();
        
        try {
            // Factura 1 - Pagada
            Factura f1 = new Factura();
            f1.setNumeroFactura("FAC-2024-001");
            f1.setCliente(new DatosCliente("Juan Pérez García", "12345678A", "Calle Mayor 123, Madrid"));
            f1.setFechaEmision(LocalDate.of(2024, 1, 15));
            f1.setItems(new ArrayList<>());
            f1.getItems().add(new ItemFactura("Aire Acondicionado Split 3000 frigorías", 1, 650.0, 21));
            f1.getItems().add(new ItemFactura("Instalación completa", 1, 350.0, 21));
            f1.recalcularTotales();
            f1.setEstado(EstadoFactura.PAGADA);
            f1.setMetodoPago(MetodoPago.TARJETA);
            f1.setNotas("Cliente satisfecho. Instalación completada sin incidencias.");
            service.insert(f1);
            logger.info("Factura 1 creada");
            
            // Factura 2 - Pendiente
            Factura f2 = new Factura();
            f2.setNumeroFactura("FAC-2024-002");
            f2.setCliente(new DatosCliente("María González López", "87654321B", "Avenida Constitución 45, Barcelona"));
            f2.setFechaEmision(LocalDate.of(2024, 2, 20));
            f2.setItems(new ArrayList<>());
            f2.getItems().add(new ItemFactura("Aire Acondicionado Multi-split 2x1", 1, 1250.0, 21));
            f2.getItems().add(new ItemFactura("Instalación Multi-split", 1, 550.0, 21));
            f2.recalcularTotales();
            f2.setEstado(EstadoFactura.PENDIENTE);
            f2.setMetodoPago(MetodoPago.TRANSFERENCIA);
            f2.setNotas("Pendiente de pago.");
            service.insert(f2);
            logger.info("Factura 2 creada");
            
            // Factura 3 - Pagada
            Factura f3 = new Factura();
            f3.setNumeroFactura("FAC-2024-003");
            f3.setCliente(new DatosCliente("Carlos Rodríguez Sánchez", "11223344C", "Plaza España 8, Valencia"));
            f3.setFechaEmision(LocalDate.of(2024, 3, 10));
            f3.setItems(new ArrayList<>());
            f3.getItems().add(new ItemFactura("Aire Acondicionado Portátil 2500 frigorías", 2, 350.0, 21));
            f3.recalcularTotales();
            f3.setEstado(EstadoFactura.PAGADA);
            f3.setMetodoPago(MetodoPago.EFECTIVO);
            f3.setNotas("Venta de equipos portátiles.");
            service.insert(f3);
            logger.info("Factura 3 creada");
            
            // Factura 4 - Pendiente
            Factura f4 = new Factura();
            f4.setNumeroFactura("FAC-2024-004");
            f4.setCliente(new DatosCliente("Ana Martínez Ruiz", "99887766D", "Calle Libertad 67, Sevilla"));
            f4.setFechaEmision(LocalDate.of(2024, 4, 5));
            f4.setItems(new ArrayList<>());
            f4.getItems().add(new ItemFactura("Aire Acondicionado Cassette 4500 frigorías", 1, 980.0, 21));
            f4.getItems().add(new ItemFactura("Instalación tipo cassette", 1, 420.0, 21));
            f4.recalcularTotales();
            f4.setEstado(EstadoFactura.PENDIENTE);
            f4.setMetodoPago(MetodoPago.FINANCIADO);
            f4.setNotas("Financiación a 12 meses.");
            service.insert(f4);
            logger.info("Factura 4 creada");
            
            // Factura 5 - Pagada
            Factura f5 = new Factura();
            f5.setNumeroFactura("FAC-2024-005");
            f5.setCliente(new DatosCliente("Luis Fernández Torres", "55443322E", "Paseo Marítimo 12, Málaga"));
            f5.setFechaEmision(LocalDate.of(2024, 5, 18));
            f5.setItems(new ArrayList<>());
            f5.getItems().add(new ItemFactura("Sistema VRV 4 unidades interiores", 1, 2500.0, 21));
            f5.getItems().add(new ItemFactura("Instalación sistema VRV completo", 1, 850.0, 21));
            f5.recalcularTotales();
            f5.setEstado(EstadoFactura.PAGADA);
            f5.setMetodoPago(MetodoPago.TRANSFERENCIA);
            f5.setNotas("Proyecto comercial.");
            service.insert(f5);
            logger.info("Factura 5 creada");
            
            // Factura 6 - Pagada
            Factura f6 = new Factura();
            f6.setNumeroFactura("FAC-2024-006");
            f6.setCliente(new DatosCliente("Isabel Moreno Díaz", "66778899F", "Calle Sol 34, Zaragoza"));
            f6.setFechaEmision(LocalDate.of(2024, 6, 22));
            f6.setItems(new ArrayList<>());
            f6.getItems().add(new ItemFactura("Aire Acondicionado Inverter 3500 frigorías", 1, 750.0, 21));
            f6.getItems().add(new ItemFactura("Instalación estándar", 1, 300.0, 21));
            f6.getItems().add(new ItemFactura("Mantenimiento anual", 1, 120.0, 21));
            f6.recalcularTotales();
            f6.setEstado(EstadoFactura.PAGADA);
            f6.setMetodoPago(MetodoPago.BIZUM);
            f6.setNotas("Incluye contrato de mantenimiento anual.");
            service.insert(f6);
            logger.info("Factura 6 creada");
            
            logger.info("✅ Todas las facturas de muestra creadas exitosamente");
            
        } catch (Exception e) {
            logger.error("Error creando facturas de muestra: ", e);
        }
    }
    
    public static void main(String[] args) {
        logger.info("Iniciando creación de facturas de muestra...");
        createSampleInvoices();
        logger.info("Proceso completado");
        System.exit(0);
    }
}
