package com.garantias.controller;

import com.garantias.model.Garantia;
import com.garantias.model.Garantia.*;
import com.garantias.service.GarantiaService;
import com.garantias.util.SessionManager;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.bson.types.ObjectId;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador para la gestión de Garantías
 */
public class GarantiasController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(GarantiasController.class);
    
    @FXML private TextField searchField;
    @FXML private ComboBox<EstadoGarantia> filterEstado;
    @FXML private Button btnNueva;
    @FXML private Button btnRefresh;
    @FXML private TableView<Garantia> garantiasTable;
    @FXML private TableColumn<Garantia, String> colNumero;
    @FXML private TableColumn<Garantia, String> colCliente;
    @FXML private TableColumn<Garantia, String> colEquipo;
    @FXML private TableColumn<Garantia, String> colFechaInicio;
    @FXML private TableColumn<Garantia, String> colFechaFin;
    @FXML private TableColumn<Garantia, String> colEstado;
    @FXML private TableColumn<Garantia, Void> colAcciones;
    @FXML private Label totalLabel;
    
    private GarantiaService garantiaService;
    private ObservableList<Garantia> garantiasList;
    private FilteredList<Garantia> filteredList;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        garantiaService = new GarantiaService();
        garantiasList = FXCollections.observableArrayList();
        
        setupTable();
        setupFilters();
        loadData();
    }
    
    private void setupTable() {
        // Número de garantía
        colNumero.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getNumeroGarantia()));
        
        // Cliente
        colCliente.setCellValueFactory(data -> {
            Cliente cliente = data.getValue().getCliente();
            return new SimpleStringProperty(cliente != null ? cliente.getNombre() : "");
        });
        
        // Equipo
        colEquipo.setCellValueFactory(data -> {
            AireAcondicionado aire = data.getValue().getAireAcondicionado();
            if (aire != null) {
                return new SimpleStringProperty(aire.getMarca() + " " + aire.getModelo());
            }
            return new SimpleStringProperty("");
        });
        
        // Fecha inicio
        colFechaInicio.setCellValueFactory(data -> {
            DetalleGarantia detalle = data.getValue().getGarantia();
            if (detalle != null && detalle.getFechaInicio() != null) {
                return new SimpleStringProperty(detalle.getFechaInicio().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        
        // Fecha fin
        colFechaFin.setCellValueFactory(data -> {
            DetalleGarantia detalle = data.getValue().getGarantia();
            if (detalle != null && detalle.getFechaFin() != null) {
                return new SimpleStringProperty(detalle.getFechaFin().format(dateFormatter));
            }
            return new SimpleStringProperty("");
        });
        
        // Estado con color
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Garantia garantia = getTableRow().getItem();
                    DetalleGarantia detalle = garantia.getGarantia();
                    if (detalle != null && detalle.getEstado() != null) {
                        Label badge = new Label(detalle.getEstado().getDisplayName());
                        badge.getStyleClass().addAll("status-badge", "status-" + detalle.getEstado().name().toLowerCase());
                        setGraphic(badge);
                    }
                }
            }
        });
        colEstado.setCellValueFactory(data -> {
            DetalleGarantia detalle = data.getValue().getGarantia();
            return new SimpleStringProperty(detalle != null && detalle.getEstado() != null ? detalle.getEstado().name() : "");
        });
        
        // Acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button();
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox actions = new HBox(5);
            
            {
                FontIcon viewIcon = new FontIcon("fas-eye");
                viewIcon.setIconSize(14);
                btnView.setGraphic(viewIcon);
                btnView.getStyleClass().addAll("btn-icon", "btn-view");
                btnView.setTooltip(new Tooltip("Ver detalles"));
                
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                btnEdit.setGraphic(editIcon);
                btnEdit.getStyleClass().addAll("btn-icon", "btn-edit");
                btnEdit.setTooltip(new Tooltip("Editar"));
                
                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                btnDelete.setGraphic(deleteIcon);
                btnDelete.getStyleClass().addAll("btn-icon", "btn-delete");
                btnDelete.setTooltip(new Tooltip("Eliminar"));
                
                actions.setAlignment(Pos.CENTER);
                actions.getChildren().addAll(btnView, btnEdit, btnDelete);
                
                btnView.setOnAction(e -> viewGarantia(getTableRow().getItem()));
                btnEdit.setOnAction(e -> editGarantia(getTableRow().getItem()));
                btnDelete.setOnAction(e -> deleteGarantia(getTableRow().getItem()));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });
        
        // Row factory para estilos alternados
        garantiasTable.setRowFactory(tv -> {
            TableRow<Garantia> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewGarantia(row.getItem());
                }
            });
            return row;
        });
    }
    
    private void setupFilters() {
        // Filtro por estado
        filterEstado.getItems().add(null); // Opción "Todos"
        filterEstado.getItems().addAll(EstadoGarantia.values());
        filterEstado.setConverter(new StringConverter<>() {
            @Override
            public String toString(EstadoGarantia estado) {
                return estado == null ? "Todos los estados" : estado.getDisplayName();
            }
            @Override
            public EstadoGarantia fromString(String string) { return null; }
        });
        filterEstado.setValue(null);
        filterEstado.setOnAction(e -> applyFilters());
        
        // Búsqueda
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
    }
    
    private void applyFilters() {
        if (filteredList == null) return;
        
        String searchText = searchField.getText().toLowerCase().trim();
        EstadoGarantia estadoFilter = filterEstado.getValue();
        
        filteredList.setPredicate(garantia -> {
            // Filtro de estado
            if (estadoFilter != null) {
                DetalleGarantia detalle = garantia.getGarantia();
                if (detalle == null || detalle.getEstado() != estadoFilter) {
                    return false;
                }
            }
            
            // Búsqueda de texto
            if (!searchText.isEmpty()) {
                boolean matchNumero = garantia.getNumeroGarantia() != null && 
                    garantia.getNumeroGarantia().toLowerCase().contains(searchText);
                boolean matchCliente = garantia.getCliente() != null && 
                    garantia.getCliente().getNombre() != null &&
                    garantia.getCliente().getNombre().toLowerCase().contains(searchText);
                boolean matchEquipo = garantia.getAireAcondicionado() != null &&
                    (garantia.getAireAcondicionado().getMarca() + " " + garantia.getAireAcondicionado().getModelo())
                    .toLowerCase().contains(searchText);
                
                return matchNumero || matchCliente || matchEquipo;
            }
            
            return true;
        });
        
        updateTotal();
    }
    
    private void loadData() {
        new Thread(() -> {
            try {
                List<Garantia> garantias = garantiaService.findAll();
                Platform.runLater(() -> {
                    garantiasList.setAll(garantias);
                    filteredList = new FilteredList<>(garantiasList, p -> true);
                    garantiasTable.setItems(filteredList);
                    updateTotal();
                });
            } catch (Exception e) {
                logger.error("Error al cargar garantías: {}", e.getMessage());
                Platform.runLater(() -> showError("Error al cargar datos", e.getMessage()));
            }
        }).start();
    }
    
    @FXML
    private void refreshData() {
        loadData();
    }
    
    @FXML
    private void showNewGarantiaDialog() {
        Dialog<Garantia> dialog = createGarantiaDialog(null);
        Optional<Garantia> result = dialog.showAndWait();
        result.ifPresent(garantia -> {
            ObjectId id = garantiaService.insert(garantia);
            garantia.setId(id);
            logger.info("Garantía creada con ID: {}", id);
            loadData();
            showSuccess("Garantía creada correctamente");
        });
    }
    
    private void viewGarantia(Garantia garantia) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Garantía");
        alert.setHeaderText(garantia.getNumeroGarantia());
        
        StringBuilder content = new StringBuilder();
        content.append("Cliente: ").append(garantia.getCliente().getNombre()).append("\n");
        content.append("Teléfono: ").append(garantia.getCliente().getTelefono()).append("\n");
        content.append("Email: ").append(garantia.getCliente().getEmail()).append("\n\n");
        
        content.append("Equipo: ").append(garantia.getAireAcondicionado().getMarca())
               .append(" ").append(garantia.getAireAcondicionado().getModelo()).append("\n");
        content.append("Nº Serie: ").append(garantia.getAireAcondicionado().getNumeroSerie()).append("\n");
        content.append("Potencia: ").append(garantia.getAireAcondicionado().getPotenciaBTU()).append(" BTU\n\n");
        
        content.append("Estado: ").append(garantia.getGarantia().getEstado().getDisplayName()).append("\n");
        content.append("Duración: ").append(garantia.getGarantia().getDuracionMeses()).append(" meses\n");
        content.append("Días restantes: ").append(garantia.getDiasRestantes()).append("\n");
        
        alert.setContentText(content.toString());
        alert.showAndWait();
    }
    
    private void editGarantia(Garantia garantia) {
        Dialog<Garantia> dialog = createGarantiaDialog(garantia);
        Optional<Garantia> result = dialog.showAndWait();
        result.ifPresent(updated -> {
            garantiaService.update(updated);
            loadData();
            showSuccess("Garantía actualizada correctamente");
        });
    }
    
    private void deleteGarantia(Garantia garantia) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Garantía");
        confirm.setHeaderText("¿Estás seguro de eliminar esta garantía?");
        confirm.setContentText("Esta acción no se puede deshacer.\n\n" + garantia.getNumeroGarantia());
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            garantiaService.delete(garantia.getId());
            loadData();
            showSuccess("Garantía eliminada correctamente");
        }
    }
    
    private Dialog<Garantia> createGarantiaDialog(Garantia existing) {
        Dialog<Garantia> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nueva Garantía" : "Editar Garantía");
        dialog.setHeaderText(existing == null ? "Crear nueva garantía" : "Modificar garantía existente");
        
        // Botones
        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Campos de cliente
        TextField clienteNombre = new TextField();
        clienteNombre.setPromptText("Nombre completo");
        TextField clienteTelefono = new TextField();
        clienteTelefono.setPromptText("+34 612 345 678");
        TextField clienteEmail = new TextField();
        clienteEmail.setPromptText("email@ejemplo.com");
        TextField clienteDireccion = new TextField();
        clienteDireccion.setPromptText("Dirección completa");
        
        // Campos de equipo
        ComboBox<String> aireMarca = new ComboBox<>();
        aireMarca.getItems().addAll("Samsung", "Daikin", "Mitsubishi", "LG", "Fujitsu", "Panasonic", "Haier", "Otro");
        aireMarca.setEditable(true);
        TextField aireModelo = new TextField();
        TextField aireSerie = new TextField();
        ComboBox<String> aireRefrigerante = new ComboBox<>();
        aireRefrigerante.getItems().addAll("R-32", "R-410A", "R-290", "R-22");
        ComboBox<Integer> airePotencia = new ComboBox<>();
        airePotencia.getItems().addAll(9000, 12000, 18000, 24000);
        DatePicker aireInstalacion = new DatePicker(LocalDate.now());
        
        // Campos de garantía
        ComboBox<TipoGarantia> garantiaTipo = new ComboBox<>();
        garantiaTipo.getItems().addAll(TipoGarantia.values());
        garantiaTipo.setValue(TipoGarantia.COMPLETA);
        ComboBox<Integer> garantiaDuracion = new ComboBox<>();
        garantiaDuracion.getItems().addAll(12, 24, 36, 48, 60);
        garantiaDuracion.setValue(24);
        
        TextArea notas = new TextArea();
        notas.setPromptText("Notas adicionales...");
        notas.setPrefRowCount(2);
        
        // Layout
        int row = 0;
        grid.add(new Label("Datos del Cliente"), 0, row++, 2, 1);
        grid.add(new Label("Nombre:"), 0, row);
        grid.add(clienteNombre, 1, row++);
        grid.add(new Label("Teléfono:"), 0, row);
        grid.add(clienteTelefono, 1, row++);
        grid.add(new Label("Email:"), 0, row);
        grid.add(clienteEmail, 1, row++);
        grid.add(new Label("Dirección:"), 0, row);
        grid.add(clienteDireccion, 1, row++);
        
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Datos del Equipo"), 0, row++, 2, 1);
        grid.add(new Label("Marca:"), 0, row);
        grid.add(aireMarca, 1, row++);
        grid.add(new Label("Modelo:"), 0, row);
        grid.add(aireModelo, 1, row++);
        grid.add(new Label("Nº Serie:"), 0, row);
        grid.add(aireSerie, 1, row++);
        grid.add(new Label("Refrigerante:"), 0, row);
        grid.add(aireRefrigerante, 1, row++);
        grid.add(new Label("Potencia (BTU):"), 0, row);
        grid.add(airePotencia, 1, row++);
        grid.add(new Label("Instalación:"), 0, row);
        grid.add(aireInstalacion, 1, row++);
        
        grid.add(new Separator(), 0, row++, 2, 1);
        grid.add(new Label("Datos de Garantía"), 0, row++, 2, 1);
        grid.add(new Label("Tipo:"), 0, row);
        grid.add(garantiaTipo, 1, row++);
        grid.add(new Label("Duración (meses):"), 0, row);
        grid.add(garantiaDuracion, 1, row++);
        grid.add(new Label("Notas:"), 0, row);
        grid.add(notas, 1, row++);
        
        // Prellenar si es edición
        if (existing != null) {
            if (existing.getCliente() != null) {
                clienteNombre.setText(existing.getCliente().getNombre());
                clienteTelefono.setText(existing.getCliente().getTelefono());
                clienteEmail.setText(existing.getCliente().getEmail());
                clienteDireccion.setText(existing.getCliente().getDireccion());
            }
            if (existing.getAireAcondicionado() != null) {
                aireMarca.setValue(existing.getAireAcondicionado().getMarca());
                aireModelo.setText(existing.getAireAcondicionado().getModelo());
                aireSerie.setText(existing.getAireAcondicionado().getNumeroSerie());
                aireRefrigerante.setValue(existing.getAireAcondicionado().getTipoRefrigerante());
                airePotencia.setValue(existing.getAireAcondicionado().getPotenciaBTU());
                aireInstalacion.setValue(existing.getAireAcondicionado().getFechaInstalacion());
            }
            if (existing.getGarantia() != null) {
                garantiaTipo.setValue(existing.getGarantia().getTipo());
                garantiaDuracion.setValue(existing.getGarantia().getDuracionMeses());
            }
            notas.setText(existing.getNotas());
        }
        
        dialog.getDialogPane().setContent(grid);
        
        // Result converter
        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                Garantia g = existing != null ? existing : new Garantia();
                
                if (existing == null) {
                    g.setNumeroGarantia(garantiaService.generateNextNumero());
                    g.setCreadoPor(SessionManager.getInstance().getCurrentUserName());
                }
                
                // Cliente
                Cliente cliente = new Cliente(
                    clienteNombre.getText(),
                    clienteTelefono.getText(),
                    clienteEmail.getText(),
                    clienteDireccion.getText()
                );
                g.setCliente(cliente);
                
                // Aire
                AireAcondicionado aire = new AireAcondicionado(
                    aireMarca.getValue(),
                    aireModelo.getText(),
                    aireSerie.getText(),
                    aireRefrigerante.getValue(),
                    airePotencia.getValue(),
                    aireInstalacion.getValue()
                );
                g.setAireAcondicionado(aire);
                
                // Garantía
                DetalleGarantia detalle = new DetalleGarantia(
                    aireInstalacion.getValue(),
                    garantiaDuracion.getValue(),
                    garantiaTipo.getValue(),
                    Arrays.asList("compresor", "evaporador", "condensador", "mano_obra")
                );
                g.setGarantia(detalle);
                
                g.setNotas(notas.getText());
                
                return g;
            }
            return null;
        });
        
        return dialog;
    }
    
    private void updateTotal() {
        int total = filteredList != null ? filteredList.size() : 0;
        totalLabel.setText("Mostrando " + total + " garantía(s)");
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
