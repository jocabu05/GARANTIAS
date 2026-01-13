package com.garantias.util;

import com.garantias.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Gestor de vistas para navegación entre pantallas
 */
public class ViewManager {
    
    private static final Logger logger = LoggerFactory.getLogger(ViewManager.class);
    private static final String VIEWS_PATH = "/views/";
    private static final String STYLES_PATH = "/styles/styles.css";
    
    /**
     * Carga una escena desde un archivo FXML
     */
    public static Scene loadScene(String viewName) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(VIEWS_PATH + viewName + ".fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1280, 720); // Tamaño completo de ventana
        scene.getStylesheets().add(ViewManager.class.getResource(STYLES_PATH).toExternalForm());
        return scene;
    }
    
    /**
     * Carga una vista y devuelve el loader (para acceder al controlador)
     */
    public static FXMLLoader getLoader(String viewName) {
        return new FXMLLoader(ViewManager.class.getResource(VIEWS_PATH + viewName + ".fxml"));
    }
    
    /**
     * Navega a una nueva vista
     */
    public static void navigateTo(String viewName) {
        try {
            Scene scene = loadScene(viewName);
            Stage stage = App.getPrimaryStage();
            stage.setScene(scene);
            logger.info("Navegando a: {}", viewName);
        } catch (IOException e) {
            logger.error("Error al cargar vista {}: {}", viewName, e.getMessage());
            throw new RuntimeException("No se pudo cargar la vista: " + viewName, e);
        }
    }
    
    /**
     * Navega al dashboard (vista principal después del login)
     */
    public static void navigateToDashboard() {
        navigateTo("dashboard");
        App.getPrimaryStage().setMaximized(true);
    }
    
    /**
     * Navega a la pantalla de login
     */
    public static void navigateToLogin() {
        navigateTo("login");
        App.getPrimaryStage().setMaximized(false);
        App.getPrimaryStage().setWidth(1280);
        App.getPrimaryStage().setHeight(720);
        App.getPrimaryStage().centerOnScreen();
    }
}
