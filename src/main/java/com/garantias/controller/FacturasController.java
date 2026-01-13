package com.garantias.controller;

import com.garantias.model.Factura;
import com.garantias.model.Factura.*;
import com.garantias.service.FacturaService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;
import org.bson.types.ObjectId;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador para la gestión de Facturas
 */
public class FacturasController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(FacturasController.class);
    
    @FXML private TextField searchField;
    @FXML private ComboBox<EstadoFactura> filterEstado;
    @FXML private DatePicker dateFrom;
    @FXML private DatePicker dateTo;
    @FXML private Button btnNueva;
    @FXML private Button btnRefresh;
    @FXML private TableView<Factura> facturasTable;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colTotal;
    @FXML private TableColumn<Factura, String> colEstado;
    @FXML private TableColumn<Factura, Void> colAcciones;
    @FXML private Label totalLabel;
    @FXML private Label sumLabel;
    
    private FacturaService facturaService;
    private ObservableList<Factura> facturasList;
    private FilteredList<Factura> filteredList;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        facturaService = new FacturaService();
        facturasList = FXCollections.observableArrayList();
        
        setupTable();
        setupFilters();
        loadData();
    }
    
    private void setupTable() {
        // Número
        colNumero.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNumeroFactura()));
        
        // Cliente
        colCliente.setCellValueFactory(data -> {
            DatosCliente cliente = data.getValue().getCliente();
            return new SimpleStringProperty(cliente != null ? cliente.getNombre() : "");
        });
        
        // Fecha
        colFecha.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFechaEmision();
            return new SimpleStringProperty(fecha != null ? fecha.format(dateFormatter) : "");
        });
        
        // Total
        colTotal.setCellValueFactory(data -> {
            Double total = data.getValue().getTotal();
            return new SimpleStringProperty(total != null ? currencyFormat.format(total) : "€0.00");
        });
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        // Estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Factura factura = getTableRow().getItem();
                    EstadoFactura estado = factura.getEstado();
                    if (estado != null) {
                        Label badge = new Label(estado.getDisplayName());
                        badge.getStyleClass().addAll("status-badge", "status-" + estado.name().toLowerCase());
                        setGraphic(badge);
                    }
                }
            }
        });
        colEstado.setCellValueFactory(data -> {
            EstadoFactura estado = data.getValue().getEstado();
            return new SimpleStringProperty(estado != null ? estado.name() : "");
        });
        
        // Acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button();
            private final Button btnPdf = new Button();
            private final Button btnDelete = new Button();
            private final HBox actions = new HBox(5);
            
            {
                FontIcon viewIcon = new FontIcon("fas-eye");
                viewIcon.setIconSize(14);
                btnView.setGraphic(viewIcon);
                btnView.getStyleClass().addAll("btn-icon", "btn-view");
                btnView.setTooltip(new Tooltip("Ver detalles"));
                
                FontIcon pdfIcon = new FontIcon("fas-file-pdf");
                pdfIcon.setIconSize(14);
                btnPdf.setGraphic(pdfIcon);
                btnPdf.getStyleClass().addAll("btn-icon", "btn-pdf");
                btnPdf.setTooltip(new Tooltip("Generar PDF"));
                
                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                btnDelete.setGraphic(deleteIcon);
                btnDelete.getStyleClass().addAll("btn-icon", "btn-delete");
                btnDelete.setTooltip(new Tooltip("Eliminar"));
                
                actions.setAlignment(Pos.CENTER);
                actions.getChildren().addAll(btnView, btnPdf, btnDelete);
                
                btnView.setOnAction(e -> viewFactura(getTableRow().getItem()));
                btnPdf.setOnAction(e -> generatePdf(getTableRow().getItem()));
                btnDelete.setOnAction(e -> deleteFactura(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });
    }
    
    private void setupFilters() {
        // Estado
        filterEstado.getItems().add(null);
        filterEstado.getItems().addAll(EstadoFactura.values());
        filterEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(EstadoFactura estado) {
                return estado == null ? "Todos" : estado.getDisplayName();
            }
            @Override
            public EstadoFactura fromString(String s) { return null; }
        });
        filterEstado.setOnAction(e -> applyFilters());
        
        // Fechas
        dateFrom.setOnAction(e -> applyFilters());
        dateTo.setOnAction(e -> applyFilters());
        
        // Búsqueda
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }
    
    private void applyFilters() {
        if (filteredList == null) return;
        
        String searchText = searchField.getText().toLowerCase().trim();
        EstadoFactura estadoFilter = filterEstado.getValue();
        LocalDate from = dateFrom.getValue();
        LocalDate to = dateTo.getValue();
        
        filteredList.setPredicate(factura -> {
            // Estado
            if (estadoFilter != null && factura.getEstado() != estadoFilter) {
                return false;
            }
            
            // Fechas
            if (from != null && factura.getFechaEmision() != null && factura.getFechaEmision().isBefore(from)) {
                return false;
            }
            if (to != null && factura.getFechaEmision() != null && factura.getFechaEmision().isAfter(to)) {
                return false;
            }
            
            // Texto
            if (!searchText.isEmpty()) {
                boolean matchNumero = factura.getNumeroFactura() != null && 
                    factura.getNumeroFactura().toLowerCase().contains(searchText);
                boolean matchCliente = factura.getCliente() != null && 
                    factura.getCliente().getNombre() != null &&
                    factura.getCliente().getNombre().toLowerCase().contains(searchText);
                return matchNumero || matchCliente;
            }
            
            return true;
        });
        
        updateTotals();
    }
    
    private void loadData() {
        new Thread(() -> {
            try {
                List<Factura> facturas = facturaService.findAll();
                Platform.runLater(() -> {
                    facturasList.setAll(facturas);
                    filteredList = new FilteredList<>(facturasList, p -> true);
                    facturasTable.setItems(filteredList);
                    updateTotals();
                });
            } catch (Exception e) {
                logger.error("Error al cargar facturas: {}", e.getMessage());
            }
        }).start();
    }
    
    @FXML
    private void refreshData() {
        loadData();
    }
    
    @FXML
    private void showNewFacturaDialog() {
        Dialog<Factura> dialog = createFacturaDialog(null);
        Optional<Factura> result = dialog.showAndWait();
        result.ifPresent(factura -> {
            ObjectId id = facturaService.insert(factura);
            factura.setId(id);
            logger.info("Factura creada con ID: {}", id);
            loadData();
            showSuccess("Factura creada correctamente");
        });
    }
    
    private void viewFactura(Factura factura) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Factura");
        alert.setHeaderText(factura.getNumeroFactura());
        
        StringBuilder content = new StringBuilder();
        content.append("Cliente: ").append(factura.getCliente().getNombre()).append("\n");
        content.append("NIF: ").append(factura.getCliente().getNif()).append("\n");
        content.append("Fecha: ").append(factura.getFechaEmision().format(dateFormatter)).append("\n\n");
        
        content.append("--- Items ---\n");
        for (ItemFactura item : factura.getItems()) {
            content.append("• ").append(item.getDescripcion())
                   .append(" x").append(item.getCantidad())
                   .append(" = ").append(currencyFormat.format(item.getTotal())).append("\n");
        }
        
        content.append("\nSubtotal: ").append(currencyFormat.format(factura.getSubtotal())).append("\n");
        content.append("IVA: ").append(currencyFormat.format(factura.getTotalIVA())).append("\n");
        content.append("TOTAL: ").append(currencyFormat.format(factura.getTotal())).append("\n");
        content.append("\nEstado: ").append(factura.getEstado().getDisplayName());
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void generatePdf(Factura factura) {
        showSuccess("Funcionalidad de generación de PDF en desarrollo.\n\nFactura: " + factura.getNumeroFactura());
    }
    
    private void deleteFactura(Factura factura) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Factura");
        confirm.setHeaderText("¿Estás seguro de eliminar esta factura?");
        confirm.setContentText(factura.getNumeroFactura());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            facturaService.delete(factura.getId());
            loadData();
            showSuccess("Factura eliminada");
        }
    }
    
    private Dialog<Factura> createFacturaDialog(Factura existing) {
        Dialog<Factura> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nueva Factura" : "Editar Factura");
        
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        TextField clienteNombre = new TextField();
        TextField clienteNif = new TextField();
        TextField clienteDireccion = new TextField();
        DatePicker fechaEmision = new DatePicker(LocalDate.now());
        ComboBox<MetodoPago> metodoPago = new ComboBox<>();
        metodoPago.getItems().addAll(MetodoPago.values());
        metodoPago.setValue(MetodoPago.TARJETA);
        
        // Items simplificado
        TextField itemDescripcion = new TextField();
        itemDescripcion.setPromptText("Descripción del producto/servicio");
        TextField itemPrecio = new TextField();
        itemPrecio.setPromptText("Precio sin IVA");
        
        int row = 0;
        grid.add(new Label("Cliente:"), 0, row);
        grid.add(clienteNombre, 1, row++);
        grid.add(new Label("NIF:"), 0, row);
        grid.add(clienteNif, 1, row++);
        grid.add(new Label("Dirección:"), 0, row);
        grid.add(clienteDireccion, 1, row++);
        grid.add(new Label("Fecha:"), 0, row);
        grid.add(fechaEmision, 1, row++);
        grid.add(new Label("Método pago:"), 0, row);
        grid.add(metodoPago, 1, row++);
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Concepto:"), 0, row);
        grid.add(itemDescripcion, 1, row++);
        grid.add(new Label("Importe (€):"), 0, row);
        grid.add(itemPrecio, 1, row++);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                Factura f = new Factura();
                f.setNumeroFactura(facturaService.generateNextNumero());
                f.setCliente(new DatosCliente(
                    clienteNombre.getText(),
                    clienteNif.getText(),
                    clienteDireccion.getText()
                ));
                f.setFechaEmision(fechaEmision.getValue());
                f.setMetodoPago(metodoPago.getValue());
                f.setEstado(EstadoFactura.PENDIENTE);
                
                try {
                    double precio = Double.parseDouble(itemPrecio.getText().replace(",", "."));
                    ItemFactura item = new ItemFactura(itemDescripcion.getText(), 1, precio, 21);
                    f.addItem(item);
                } catch (NumberFormatException e) {
                    // Ignorar items inválidos
                }
                
                return f;
            }
            return null;
        });
        
        return dialog;
    }
    
    private void updateTotals() {
        int count = filteredList != null ? filteredList.size() : 0;
        totalLabel.setText("Mostrando " + count + " factura(s)");
        
        double sum = 0;
        if (filteredList != null) {
            for (Factura f : filteredList) {
                if (f.getTotal() != null) {
                    sum += f.getTotal();
                }
            }
        }
        sumLabel.setText("Total: " + currencyFormat.format(sum));
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
