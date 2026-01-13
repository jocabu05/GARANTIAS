package com.garantias.controller;

import com.garantias.config.OdooConfig;
import com.garantias.model.Garantia;
import com.garantias.model.Garantia.EstadoGarantia;
import com.garantias.service.FacturaService;
import com.garantias.service.GarantiaService;
import com.garantias.util.SessionManager;
import com.garantias.util.ViewManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador para el Dashboard principal
 */
public class DashboardController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    // Sidebar
    @FXML private VBox sidebar;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Circle userAvatar;
    @FXML private Button btnDashboard;
    @FXML private Button btnGarantias;
    @FXML private Button btnFacturas;
    @FXML private Button btnGraficas;
    @FXML private Button btnLogout;
    
    // Content
    @FXML private StackPane contentArea;
    @FXML private Label pageTitle;
    @FXML private Label dateLabel;
    
    // Dashboard Stats
    @FXML private VBox dashboardContent;
    @FXML private Label statGarantiasActivas;
    @FXML private Label statGarantiasVencer;
    @FXML private Label statFacturasTotal;
    @FXML private Label statIngresosMes;
    @FXML private VBox alertsContainer;
    @FXML private VBox recentContainer;
    @FXML private PieChart estadoChart;
    @FXML private BarChart<String, Number> marcasChart;
    
    private GarantiaService garantiaService;
    private FacturaService facturaService;
    private Button currentActiveButton;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializar servicios
        initServices();
        
        // Configurar usuario
        setupUserInfo();
        
        // Configurar fecha
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", new Locale("es", "ES"))));
        
        // Configurar navegación
        setupNavigation();
        
        // Cargar dashboard
        showDashboard();
        
        // Animación de entrada
        animateEntrance();
    }
    
    private void initServices() {
        try {
            garantiaService = new GarantiaService();
            facturaService = new FacturaService();
        } catch (Exception e) {
            logger.error("Error al inicializar servicios: {}", e.getMessage());
            showAlert("Error", "No se pudo conectar a la base de datos. Verifique que Docker esté en ejecución.", Alert.AlertType.ERROR);
        }
    }
    
    private void setupUserInfo() {
        String userName = SessionManager.getInstance().getCurrentUserName();
        userNameLabel.setText(userName);
        userRoleLabel.setText("Administrador");
        
        // Avatar con iniciales
        String initials = SessionManager.getInstance().getCurrentUser() != null 
            ? SessionManager.getInstance().getCurrentUser().getInitials() 
            : "AD";
    }
    
    private void setupNavigation() {
        btnDashboard.setOnAction(e -> showDashboard());
        btnGarantias.setOnAction(e -> showGarantias());
        btnFacturas.setOnAction(e -> showFacturas());
        btnGraficas.setOnAction(e -> showGraficas());
        btnLogout.setOnAction(e -> handleLogout());
        
        setActiveButton(btnDashboard);
    }
    
    private void setActiveButton(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        currentActiveButton = button;
    }
    
    // ========================================
    // DASHBOARD
    // ========================================
    @FXML
    private void showDashboard() {
        setActiveButton(btnDashboard);
        pageTitle.setText("Dashboard");
        
        loadView("dashboard_content");
        loadDashboardStats();
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
                Map<String, Long> marcaCounts = garantiaService.countByMarca();
                
                Platform.runLater(() -> {
                    // Actualizar stats cards
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
                        statIngresosMes.setText(NumberFormat.getCurrencyInstance(new Locale("es", "ES")).format(totalFacturado));
                    }
                    
                    // Actualizar alertas
                    updateAlerts(proximasVencer);
                    
                    // Actualizar gráficas
                    updateCharts(estadoCounts, marcaCounts);
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
                new KeyFrame(Duration.millis(i * 30), e -> label.setText(String.valueOf(value)))
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
        item.setPadding(new Insets(10));
        
        FontIcon icon = new FontIcon("fas-exclamation-triangle");
        icon.setIconColor(Color.web("#FF9800"));
        icon.setIconSize(16);
        
        VBox info = new VBox(2);
        Label title = new Label(g.getCliente().getNombre());
        title.getStyleClass().add("alert-title");
        Label subtitle = new Label(g.getNumeroGarantia() + " - Vence en " + g.getDiasRestantes() + " días");
        subtitle.getStyleClass().add("alert-subtitle");
        info.getChildren().addAll(title, subtitle);
        
        HBox.setHgrow(info, Priority.ALWAYS);
        item.getChildren().addAll(icon, info);
        
        return item;
    }
    
    private void updateCharts(Map<EstadoGarantia, Long> estadoCounts, Map<String, Long> marcaCounts) {
        // Gráfica de estados
        if (estadoChart != null) {
            estadoChart.getData().clear();
            for (Map.Entry<EstadoGarantia, Long> entry : estadoCounts.entrySet()) {
                if (entry.getValue() > 0) {
                    PieChart.Data slice = new PieChart.Data(entry.getKey().getDisplayName(), entry.getValue());
                    estadoChart.getData().add(slice);
                }
            }
        }
        
        // Gráfica de marcas
        if (marcasChart != null) {
            marcasChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Garantías por Marca");
            for (Map.Entry<String, Long> entry : marcaCounts.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            marcasChart.getData().add(series);
        }
    }
    
    // ========================================
    // GARANTIAS
    // ========================================
    @FXML
    private void showGarantias() {
        setActiveButton(btnGarantias);
        pageTitle.setText("Gestión de Garantías");
        loadView("garantias");
    }
    
    // ========================================
    // FACTURAS
    // ========================================
    @FXML
    private void showFacturas() {
        setActiveButton(btnFacturas);
        pageTitle.setText("Gestión de Facturas");
        loadView("facturas");
    }
    
    // ========================================
    // GRÁFICAS
    // ========================================
    @FXML
    private void showGraficas() {
        setActiveButton(btnGraficas);
        pageTitle.setText("Estadísticas y Gráficas");
        loadView("graficas");
    }
    
    // ========================================
    // LOGOUT
    // ========================================
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar Sesión");
        alert.setHeaderText("¿Estás seguro de que quieres cerrar sesión?");
        alert.setContentText("Volverás a la pantalla de inicio de sesión.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            OdooConfig.logout();
            SessionManager.getInstance().logout();
            ViewManager.navigateToLogin();
        }
    }
    
    // ========================================
    // UTILIDADES
    // ========================================
    private void loadView(String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + viewName + ".fxml"));
            Parent view = loader.load();
            
            // Animación de transición
            view.setOpacity(0);
            contentArea.getChildren().setAll(view);
            
            FadeTransition fade = new FadeTransition(Duration.millis(300), view);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
            
        } catch (IOException e) {
            logger.error("Error al cargar vista {}: {}", viewName, e.getMessage());
        }
    }
    
    private void animateEntrance() {
        // Animar sidebar
        sidebar.setTranslateX(-280);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), sidebar);
        slideIn.setFromX(-280);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        slideIn.play();
        
        // Animar contenido
        contentArea.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), contentArea);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(200));
        fadeIn.play();
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
