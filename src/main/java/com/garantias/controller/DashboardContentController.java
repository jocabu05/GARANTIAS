package com.garantias.controller;

import com.garantias.model.Garantia;
import com.garantias.model.Garantia.EstadoGarantia;
import com.garantias.service.FacturaService;
import com.garantias.service.GarantiaService;
import com.garantias.util.ViewManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.bson.types.ObjectId;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

/**
 * Controlador para el contenido del Dashboard
 */
public class DashboardContentController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardContentController.class);
    
    @FXML private Label statGarantiasActivas;
    @FXML private Label statGarantiasVencer;
    @FXML private Label statFacturasTotal;
    @FXML private Label statIngresosMes;
    @FXML private VBox alertsContainer;
    @FXML private PieChart estadoChart;
    
    @FXML private Button btnNuevaGarantia;
    @FXML private Button btnNuevaFactura;
    @FXML private Button btnVerReportes;
    
    private GarantiaService garantiaService;
    private FacturaService facturaService;
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            garantiaService = new GarantiaService();
            facturaService = new FacturaService();
            loadDashboardStats();
        } catch (Exception e) {
            logger.error("Error al inicializar dashboard: {}", e.getMessage());
        }
    }
    
    private void loadDashboardStats() {
        new Thread(() -> {
            try {
                // Obtener estadísticas
                Map<EstadoGarantia, Long> estadoCounts = garantiaService.countByEstado();
                long activas = estadoCounts.getOrDefault(EstadoGarantia.ACTIVA, 0L);
                List<Garantia> proximasVencer = garantiaService.findProximasAVencer(30);
                long totalFacturas = facturaService.countTotal();
                double totalFacturado = facturaService.getTotalFacturado();
                
                Platform.runLater(() -> {
                    // Actualizar stats cards con animación
                    if (statGarantiasActivas != null) {
                        animateNumber(statGarantiasActivas, activas);
                    }
                    if (statGarantiasVencer != null) {
                        animateNumber(statGarantiasVencer, proximasVencer.size());
                    }
                    if (statFacturasTotal != null) {
                        animateNumber(statFacturasTotal, totalFacturas);
                    }
                    if (statIngresosMes != null) {
                        statIngresosMes.setText(currencyFormat.format(totalFacturado));
                    }
                    
                    // Actualizar alertas
                    updateAlerts(proximasVencer);
                    
                    // Actualizar gráfica
                    updateChart(estadoCounts);
                });
            } catch (Exception e) {
                logger.error("Error al cargar estadísticas: {}", e.getMessage());
            }
        }).start();
    }
    
    private void animateNumber(Label label, long target) {
        Timeline timeline = new Timeline();
        for (int i = 0; i <= 20; i++) {
            final long value = (target * i) / 20;
            timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(i * 25), e -> label.setText(String.valueOf(value)))
            );
        }
        timeline.play();
    }
    
    private void updateAlerts(List<Garantia> proximasVencer) {
        if (alertsContainer == null) return;
        
        alertsContainer.getChildren().clear();
        
        if (proximasVencer.isEmpty()) {
            Label noAlerts = new Label("✓ No hay garantías próximas a vencer");
            noAlerts.getStyleClass().add("no-alerts");
            noAlerts.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 13px;");
            alertsContainer.getChildren().add(noAlerts);
        } else {
            for (Garantia g : proximasVencer.subList(0, Math.min(5, proximasVencer.size()))) {
                HBox alertItem = createAlertItem(g);
                alertsContainer.getChildren().add(alertItem);
            }
        }
    }
    
    private HBox createAlertItem(Garantia g) {
        HBox item = new HBox(10);
        item.getStyleClass().add("alert-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8));
        item.setStyle("-fx-background-color: rgba(255, 152, 0, 0.1); -fx-background-radius: 8;");
        
        Label icon = new Label("⚠");
        icon.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 14px;");
        
        VBox info = new VBox(2);
        String clienteName = g.getCliente() != null ? g.getCliente().getNombre() : "Sin cliente";
        Label title = new Label(clienteName);
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label subtitle = new Label(g.getNumeroGarantia() + " - Vence en " + g.getDiasRestantes() + " días");
        subtitle.setStyle("-fx-text-fill: #9e9e9e; -fx-font-size: 11px;");
        info.getChildren().addAll(title, subtitle);
        
        HBox.setHgrow(info, Priority.ALWAYS);
        item.getChildren().addAll(icon, info);
        
        return item;
    }
    
    private void updateChart(Map<EstadoGarantia, Long> estadoCounts) {
        if (estadoChart == null) return;
        
        estadoChart.getData().clear();
        String[] colors = {"#4CAF50", "#9E9E9E", "#FF9800", "#F44336"};
        int i = 0;
        
        for (Map.Entry<EstadoGarantia, Long> entry : estadoCounts.entrySet()) {
            if (entry.getValue() > 0) {
                PieChart.Data slice = new PieChart.Data(
                    entry.getKey().getDisplayName() + " (" + entry.getValue() + ")", 
                    entry.getValue()
                );
                estadoChart.getData().add(slice);
            }
        }
        
        // Aplicar colores después de renderizar
        Platform.runLater(() -> {
            int colorIndex = 0;
            for (PieChart.Data data : estadoChart.getData()) {
                if (data.getNode() != null) {
                    String color = colors[colorIndex % colors.length];
                    data.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
                colorIndex++;
            }
        });
    }
    
    // ========================================
    // ACCIONES RÁPIDAS
    // ========================================
    
    @FXML
    private void onNuevaGarantia() {
        logger.info("Acción: Nueva Garantía");
        showNuevaGarantiaDialog();
    }
    
    @FXML
    private void onNuevaFactura() {
        logger.info("Acción: Nueva Factura");
        showNuevaFacturaDialog();
    }
    
    @FXML
    private void onVerReportes() {
        logger.info("Acción: Ver Estadísticas");
        // Cambiar a vista de estadísticas
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Estadísticas");
        info.setHeaderText("📊 Ver Estadísticas");
        info.setContentText("Haz clic en 'Estadísticas' en el menú lateral para ver los gráficos completos.");
        info.showAndWait();
    }
    
    private void showNuevaGarantiaDialog() {
        Dialog<Garantia> dialog = new Dialog<>();
        dialog.setTitle("Nueva Garantía");
        dialog.setHeaderText("Crear Nueva Garantía");
        
        // Botones
        ButtonType crearButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);
        
        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField clienteNombre = new TextField();
        clienteNombre.setPromptText("Nombre del cliente");
        TextField clienteTelefono = new TextField();
        clienteTelefono.setPromptText("Teléfono");
        TextField clienteEmail = new TextField();
        clienteEmail.setPromptText("Email");
        TextField clienteDireccion = new TextField();
        clienteDireccion.setPromptText("Dirección");
        
        TextField marca = new TextField();
        marca.setPromptText("Marca del aire");
        TextField modelo = new TextField();
        modelo.setPromptText("Modelo");
        TextField numeroSerie = new TextField();
        numeroSerie.setPromptText("Número de serie");
        
        ComboBox<String> tipoGarantia = new ComboBox<>();
        tipoGarantia.getItems().addAll("COMPLETA", "EXTENDIDA", "LIMITADA");
        tipoGarantia.setValue("COMPLETA");
        
        Spinner<Integer> duracion = new Spinner<>(6, 60, 24, 6);
        
        grid.add(new Label("Cliente:"), 0, 0);
        grid.add(clienteNombre, 1, 0);
        grid.add(new Label("Teléfono:"), 0, 1);
        grid.add(clienteTelefono, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(clienteEmail, 1, 2);
        grid.add(new Label("Dirección:"), 0, 3);
        grid.add(clienteDireccion, 1, 3);
        
        grid.add(new Label("Marca:"), 0, 4);
        grid.add(marca, 1, 4);
        grid.add(new Label("Modelo:"), 0, 5);
        grid.add(modelo, 1, 5);
        grid.add(new Label("Nº Serie:"), 0, 6);
        grid.add(numeroSerie, 1, 6);
        
        grid.add(new Label("Tipo:"), 0, 7);
        grid.add(tipoGarantia, 1, 7);
        grid.add(new Label("Duración (meses):"), 0, 8);
        grid.add(duracion, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Convertir resultado
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == crearButtonType) {
                try {
                    Garantia g = new Garantia();
                    g.setNumeroGarantia(garantiaService.generateNextNumero());
                    g.setCreadoPor(com.garantias.util.SessionManager.getInstance().getCurrentUserName());
                    
                    Garantia.Cliente cliente = new Garantia.Cliente();
                    cliente.setNombre(clienteNombre.getText());
                    cliente.setTelefono(clienteTelefono.getText());
                    cliente.setEmail(clienteEmail.getText());
                    cliente.setDireccion(clienteDireccion.getText());
                    g.setCliente(cliente);
                    
                    Garantia.AireAcondicionado aire = new Garantia.AireAcondicionado();
                    aire.setMarca(marca.getText());
                    aire.setModelo(modelo.getText());
                    aire.setNumeroSerie(numeroSerie.getText());
                    aire.setFechaInstalacion(java.time.LocalDate.now()); // Añadido
                    aire.setPotenciaBTU(12000); // Valor por defecto
                    aire.setTipoRefrigerante("R-32"); // Valor por defecto
                    g.setAireAcondicionado(aire);
                    
                    Garantia.DetalleGarantia detalle = new Garantia.DetalleGarantia();
                    detalle.setTipo(Garantia.TipoGarantia.valueOf(tipoGarantia.getValue()));
                    detalle.setEstado(EstadoGarantia.ACTIVA);
                    detalle.setDuracionMeses(duracion.getValue());
                    detalle.setFechaInicio(java.time.LocalDate.now());
                    detalle.setFechaFin(java.time.LocalDate.now().plusMonths(duracion.getValue()));
                    detalle.setCobertura(java.util.Arrays.asList("compresor", "evaporador", "condensador")); // Añadido
                    g.setGarantia(detalle);
                    
                    return g;
                } catch (Exception e) {
                    logger.error("Error creando garantía: {}", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<Garantia> result = dialog.showAndWait();
        result.ifPresent(garantia -> {
            try {
                logger.info("Intentando guardar garantía: {}", garantia.getNumeroGarantia());
                logger.info("Cliente: {}", garantia.getCliente() != null ? garantia.getCliente().getNombre() : "null");
                logger.info("Aire: {}", garantia.getAireAcondicionado() != null ? garantia.getAireAcondicionado().getMarca() : "null");
                
                ObjectId id = garantiaService.insert(garantia);
                garantia.setId(id);
                logger.info("✅ Garantía creada exitosamente con ID: {}", id);
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Éxito");
                success.setHeaderText("Garantía creada");
                success.setContentText("La garantía " + garantia.getNumeroGarantia() + " ha sido creada correctamente.");
                success.showAndWait();
                loadDashboardStats(); // Refrescar estadísticas
            } catch (Exception e) {
                logger.error("❌ Error al crear garantía", e);
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setHeaderText("No se pudo crear la garantía");
                error.setContentText("Error: " + e.getMessage() + "\n\nRevisa los logs para más detalles.");
                error.showAndWait();
            }
        });
    }
    
    private void showNuevaFacturaDialog() {
        Dialog<com.garantias.model.Factura> dialog = new Dialog<>();
        dialog.setTitle("Nueva Factura");
        dialog.setHeaderText("Crear Nueva Factura");
        
        ButtonType crearButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(crearButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField clienteNombre = new TextField();
        clienteNombre.setPromptText("Nombre del cliente");
        TextField clienteNif = new TextField();
        clienteNif.setPromptText("NIF/CIF");
        TextField clienteDireccion = new TextField();
        clienteDireccion.setPromptText("Dirección");
        
        TextField descripcion = new TextField();
        descripcion.setPromptText("Descripción del servicio");
        Spinner<Double> precio = new Spinner<>(0.0, 10000.0, 100.0, 10.0);
        precio.setEditable(true);
        
        ComboBox<String> metodoPago = new ComboBox<>();
        metodoPago.getItems().addAll("EFECTIVO", "TARJETA", "TRANSFERENCIA");
        metodoPago.setValue("EFECTIVO");
        
        grid.add(new Label("Cliente:"), 0, 0);
        grid.add(clienteNombre, 1, 0);
        grid.add(new Label("NIF/CIF:"), 0, 1);
        grid.add(clienteNif, 1, 1);
        grid.add(new Label("Dirección:"), 0, 2);
        grid.add(clienteDireccion, 1, 2);
        grid.add(new Label("Descripción:"), 0, 3);
        grid.add(descripcion, 1, 3);
        grid.add(new Label("Precio (€):"), 0, 4);
        grid.add(precio, 1, 4);
        grid.add(new Label("Método pago:"), 0, 5);
        grid.add(metodoPago, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == crearButtonType) {
                try {
                    com.garantias.model.Factura f = new com.garantias.model.Factura();
                    f.setNumeroFactura(facturaService.generateNextNumero());
                    
                    com.garantias.model.Factura.DatosCliente cliente = new com.garantias.model.Factura.DatosCliente(
                        clienteNombre.getText(), clienteNif.getText(), clienteDireccion.getText()
                    );
                    f.setCliente(cliente);
                    
                    com.garantias.model.Factura.ItemFactura item = new com.garantias.model.Factura.ItemFactura();
                    item.setDescripcion(descripcion.getText());
                    item.setCantidad(1);
                    item.setPrecioUnitario(precio.getValue());
                    item.setIva(21);
                    // Calcular total del item manualmente
                    double itemTotal = precio.getValue() * (1 + 21.0 / 100);
                    item.setTotal(itemTotal);
                    f.setItems(java.util.Collections.singletonList(item));
                    
                    // Calcular totales manualmente
                    double subtotal = precio.getValue();
                    double iva = subtotal * 0.21;
                    f.setSubtotal(subtotal);
                    f.setTotalIVA(iva);
                    f.setTotal(subtotal + iva);
                    
                    f.setEstado(com.garantias.model.Factura.EstadoFactura.PENDIENTE);
                    f.setMetodoPago(com.garantias.model.Factura.MetodoPago.valueOf(metodoPago.getValue()));
                    f.setFechaEmision(java.time.LocalDate.now());
                    
                    return f;
                } catch (Exception e) {
                    logger.error("Error creando factura: {}", e.getMessage());
                }
            }
            return null;
        });
        
        Optional<com.garantias.model.Factura> result = dialog.showAndWait();
        result.ifPresent(factura -> {
            try {
                ObjectId id = facturaService.insert(factura);
                factura.setId(id);
                logger.info("Factura creada con ID: {}", id);
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Éxito");
                success.setHeaderText("Factura creada");
                success.setContentText("La factura " + factura.getNumeroFactura() + " por " + 
                    currencyFormat.format(factura.getTotal()) + " ha sido creada.");
                success.showAndWait();
                loadDashboardStats();
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Error");
                error.setContentText("No se pudo crear la factura: " + e.getMessage());
                error.showAndWait();
            }
        });
    }
}
