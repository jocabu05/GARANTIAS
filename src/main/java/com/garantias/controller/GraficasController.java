package com.garantias.controller;

import com.garantias.model.Garantia.EstadoGarantia;
import com.garantias.service.FacturaService;
import com.garantias.service.GarantiaService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.NumberFormat;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controlador para la vista de Gráficas y Estadísticas
 */
public class GraficasController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(GraficasController.class);
    
    @FXML private PieChart estadoChart;
    @FXML private BarChart<String, Number> marcasChart;
    @FXML private LineChart<String, Number> facturacionChart;
    @FXML private PieChart tipoGarantiaChart;
    
    @FXML private Label totalGarantias;
    @FXML private Label totalFacturas;
    @FXML private Label totalFacturado;
    @FXML private Label promedioFactura;
    
    @FXML private VBox chartsContainer;
    
    private GarantiaService garantiaService;
    private FacturaService facturaService;
    
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        garantiaService = new GarantiaService();
        facturaService = new FacturaService();
        
        loadCharts();
        loadStats();
    }
    
    private void loadCharts() {
        new Thread(() -> {
            try {
                Map<EstadoGarantia, Long> estadoCounts = garantiaService.countByEstado();
                Map<String, Long> marcaCounts = garantiaService.countByMarca();
                Map<Integer, Double> facturacionMensual = facturaService.getFacturacionPorMes();
                
                Platform.runLater(() -> {
                    // Gráfica de estados (Pie)
                    if (estadoChart != null) {
                        estadoChart.getData().clear();
                        for (Map.Entry<EstadoGarantia, Long> entry : estadoCounts.entrySet()) {
                            if (entry.getValue() > 0) {
                                PieChart.Data slice = new PieChart.Data(
                                    entry.getKey().getDisplayName() + " (" + entry.getValue() + ")", 
                                    entry.getValue()
                                );
                                estadoChart.getData().add(slice);
                            }
                        }
                        applyPieChartColors(estadoChart, estadoCounts);
                    }
                    
                    // Gráfica de marcas (Bar)
                    if (marcasChart != null) {
                        marcasChart.getData().clear();
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName("Cantidad");
                        marcaCounts.entrySet().stream()
                            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                            .limit(8)
                            .forEach(entry -> series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
                        marcasChart.getData().add(series);
                        applyBarChartColors(marcasChart);
                    }
                    
                    // Gráfica de facturación (Line)
                    if (facturacionChart != null) {
                        facturacionChart.getData().clear();
                        XYChart.Series<String, Number> series = new XYChart.Series<>();
                        series.setName("Ingresos (€)");
                        for (int mes = 1; mes <= 12; mes++) {
                            String nombreMes = Month.of(mes).getDisplayName(TextStyle.SHORT, new Locale("es", "ES"));
                            series.getData().add(new XYChart.Data<>(nombreMes, facturacionMensual.getOrDefault(mes, 0.0)));
                        }
                        facturacionChart.getData().add(series);
                    }
                    
                    // Gráfica de tipos
                    if (tipoGarantiaChart != null) {
                        tipoGarantiaChart.getData().clear();
                        tipoGarantiaChart.getData().addAll(
                            new PieChart.Data("Completa", 60),
                            new PieChart.Data("Extendida", 25),
                            new PieChart.Data("Limitada", 15)
                        );
                    }
                });
            } catch (Exception e) {
                logger.error("Error al cargar gráficas: {}", e.getMessage());
            }
        }).start();
    }
    
    private void loadStats() {
        new Thread(() -> {
            try {
                long totalG = garantiaService.countTotal();
                long totalF = facturaService.countTotal();
                double totalFac = facturaService.getTotalFacturado();
                double promedio = totalF > 0 ? totalFac / totalF : 0;
                
                Platform.runLater(() -> {
                    if (totalGarantias != null) totalGarantias.setText(String.valueOf(totalG));
                    if (totalFacturas != null) totalFacturas.setText(String.valueOf(totalF));
                    if (totalFacturado != null) totalFacturado.setText(currencyFormat.format(totalFac));
                    if (promedioFactura != null) promedioFactura.setText(currencyFormat.format(promedio));
                });
                
            } catch (Exception e) {
                logger.error("Error al cargar estadísticas: {}", e.getMessage());
            }
        }).start();
    }
    
    private void applyPieChartColors(PieChart chart, Map<EstadoGarantia, Long> counts) {
        String[] colors = {"#4CAF50", "#9E9E9E", "#FF9800", "#F44336"};
        int i = 0;
        for (PieChart.Data data : chart.getData()) {
            String color = colors[i % colors.length];
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
            i++;
        }
    }
    
    private void applyBarChartColors(BarChart<String, Number> chart) {
        String[] colors = {"#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#00BCD4", "#E91E63", "#795548", "#607D8B"};
        
        for (XYChart.Series<String, Number> series : chart.getData()) {
            int i = 0;
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    String color = colors[i % colors.length];
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
                i++;
            }
        }
    }
    
    @FXML
    private void refreshCharts() {
        loadCharts();
        loadStats();
    }
    
    @FXML
    private void exportReport() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Exportar Reporte");
        alert.setHeaderText(null);
        alert.setContentText("Funcionalidad de exportación de reportes en desarrollo.");
        alert.showAndWait();
    }
}
