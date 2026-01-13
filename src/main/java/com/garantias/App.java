 package com.garantias;

import com.garantias.config.MongoDBConfig;
import com.garantias.util.ViewManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Aplicación principal de Gestión de Garantías de Aires Acondicionados
 * 
 * @author Sistema de Garantías
 * @version 1.0.0
 */
public class App extends Application {
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        // Configurar la ventana principal
        stage.setTitle("🌬️ Sistema de Garantías - Aires Acondicionados");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        
        // Cargar la vista de login
        Scene scene = ViewManager.loadScene("login");
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
        
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    
    @Override
    public void stop() throws Exception {
        // Cerrar conexión a MongoDB al salir
        MongoDBConfig.close();
        super.stop();
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
