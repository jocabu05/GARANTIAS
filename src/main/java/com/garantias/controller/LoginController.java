package com.garantias.controller;

import com.garantias.config.OdooConfig;
import com.garantias.model.Usuario;
import com.garantias.util.SessionManager;
import com.garantias.util.ViewManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de Login
 */
public class LoginController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private VBox loginCard;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox particlesContainer;
    @FXML private CheckBox rememberCheck;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Animación de entrada del card
        loginCard.setOpacity(0);
        loginCard.setTranslateY(30);
        
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(loginCard.opacityProperty(), 0),
                new KeyValue(loginCard.translateYProperty(), 30)),
            new KeyFrame(Duration.millis(600), 
                new KeyValue(loginCard.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(loginCard.translateYProperty(), 0, Interpolator.EASE_OUT))
        );
        fadeIn.play();
        
        // Ocultar elementos
        errorLabel.setVisible(false);
        loadingIndicator.setVisible(false);
        
        // Listener para enter
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> passwordField.requestFocus());
        
        // Crear partículas decorativas
        createParticles();
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validaciones
        if (username.isEmpty()) {
            showError("Por favor, introduce tu usuario");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Por favor, introduce tu contraseña");
            passwordField.requestFocus();
            return;
        }
        
        // Mostrar loading
        setLoading(true);
        errorLabel.setVisible(false);
        
        // Autenticar en background
        new Thread(() -> {
            try {
                Integer uid = OdooConfig.authenticate(username, password);
                
                Platform.runLater(() -> {
                    if (uid != null) {
                        // Login exitoso con Odoo
                        logger.info("Login exitoso para usuario: {}", username);
                        
                        Map<String, Object> userData = OdooConfig.getCurrentUser();
                        Usuario usuario = new Usuario();
                        usuario.setId(uid);
                        usuario.setLogin(username);
                        if (userData != null) {
                            usuario.setNombre((String) userData.get("name"));
                            usuario.setEmail((String) userData.get("email"));
                        } else {
                            usuario.setNombre(username);
                        }
                        
                        SessionManager.getInstance().setCurrentUser(usuario);
                        ViewManager.navigateToDashboard();
                    } else {
                        // Probar modo demo
                        if (username.equals("admin") && password.equals("admin")) {
                            logger.info("Usando modo demo");
                            Usuario demoUser = new Usuario(1, "Administrador Demo", "admin", "admin@demo.com");
                            SessionManager.getInstance().setCurrentUser(demoUser);
                            ViewManager.navigateToDashboard();
                        } else {
                            showError("Usuario o contraseña incorrectos");
                            setLoading(false);
                            shakeCard();
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Error de conexión: {}", e.getMessage());
                Platform.runLater(() -> {
                    // Modo demo cuando Odoo no está disponible
                    if (username.equals("admin") && password.equals("admin")) {
                        logger.info("Odoo no disponible, usando modo demo");
                        Usuario demoUser = new Usuario(1, "Administrador Demo", "admin", "admin@demo.com");
                        SessionManager.getInstance().setCurrentUser(demoUser);
                        ViewManager.navigateToDashboard();
                    } else {
                        showError("Odoo no disponible. Usa: admin/admin");
                        setLoading(false);
                    }
                });
            }
        }).start();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Animación de aparición
        FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }
    
    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        usernameField.setDisable(loading);
        passwordField.setDisable(loading);
        
        if (loading) {
            loginButton.setText("Iniciando sesión...");
        } else {
            loginButton.setText("Iniciar Sesión");
        }
    }
    
    private void shakeCard() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), loginCard);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    private void createParticles() {
        if (particlesContainer == null) return;
        
        // Crear círculos flotantes decorativos
        for (int i = 0; i < 15; i++) {
            Circle circle = new Circle(Math.random() * 30 + 10);
            circle.setFill(Color.web("#ffffff", 0.05 + Math.random() * 0.1));
            circle.setTranslateX(Math.random() * 1200);
            circle.setTranslateY(Math.random() * 700);
            
            // Animación flotante
            Timeline float_ = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(circle.translateYProperty(), circle.getTranslateY())),
                new KeyFrame(Duration.seconds(3 + Math.random() * 4),
                    new KeyValue(circle.translateYProperty(), circle.getTranslateY() - 50 - Math.random() * 50))
            );
            float_.setCycleCount(Animation.INDEFINITE);
            float_.setAutoReverse(true);
            float_.play();
            
            particlesContainer.getChildren().add(circle);
        }
    }
}
