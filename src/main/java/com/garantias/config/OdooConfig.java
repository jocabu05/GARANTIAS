package com.garantias.config;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Configuración y cliente para conexión con Odoo via XML-RPC
 */
public class OdooConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(OdooConfig.class);
    
    private static final String ODOO_URL = "http://localhost:8070";
    private static final String DATABASE = "GARANTIAS";
    
    private static XmlRpcClient commonClient;
    private static XmlRpcClient objectClient;
    private static Integer uid;
    private static String currentPassword;
    
    /**
     * Autentica un usuario en Odoo
     * 
     * @param username Nombre de usuario
     * @param password Contraseña
     * @return ID del usuario si la autenticación es exitosa, null si falla
     */
    public static Integer authenticate(String username, String password) {
        try {
            XmlRpcClientConfigImpl commonConfig = new XmlRpcClientConfigImpl();
            commonConfig.setServerURL(new URL(ODOO_URL + "/xmlrpc/2/common"));
            
            commonClient = new XmlRpcClient();
            commonClient.setConfig(commonConfig);
            
            // Verificar versión de Odoo
            Object version = commonClient.execute("version", Collections.emptyList());
            logger.info("Conectado a Odoo versión: {}", version);
            
            // Autenticar usuario
            logger.info("Intentando autenticar: DB={}, User={}", DATABASE, username);
            Object result = commonClient.execute("authenticate", Arrays.asList(
                DATABASE, username, password, Collections.emptyMap()
            ));
            
            logger.info("Resultado de autenticación: {} (tipo: {})", result, result != null ? result.getClass().getName() : "null");
            
            if (result instanceof Integer && (Integer) result > 0) {
                uid = (Integer) result;
                currentPassword = password;
                
                // Configurar cliente de objetos
                XmlRpcClientConfigImpl objectConfig = new XmlRpcClientConfigImpl();
                objectConfig.setServerURL(new URL(ODOO_URL + "/xmlrpc/2/object"));
                objectClient = new XmlRpcClient();
                objectClient.setConfig(objectConfig);
                
                logger.info("✅ Usuario autenticado correctamente. UID: {}", uid);
                return uid;
            } else {
                logger.warn("❌ Autenticación fallida para usuario: {}. Resultado: {}", username, result);
                return null;
            }
            
        } catch (MalformedURLException | XmlRpcException e) {
            logger.error("Error al conectar con Odoo: {}", e.getMessage());
            throw new RuntimeException("Error de conexión con Odoo", e);
        }
    }
    
    /**
     * Ejecuta un método en un modelo de Odoo
     */
    public static Object execute(String model, String method, List<Object> args) {
        return execute(model, method, args, Collections.emptyMap());
    }
    
    /**
     * Ejecuta un método en un modelo de Odoo con kwargs
     */
    public static Object execute(String model, String method, List<Object> args, Map<String, Object> kwargs) {
        try {
            if (uid == null || objectClient == null) {
                throw new RuntimeException("No autenticado en Odoo");
            }
            
            return objectClient.execute("execute_kw", Arrays.asList(
                DATABASE, uid, currentPassword, model, method, args, kwargs
            ));
            
        } catch (XmlRpcException e) {
            logger.error("Error al ejecutar método {} en modelo {}: {}", method, model, e.getMessage());
            throw new RuntimeException("Error en operación Odoo", e);
        }
    }
    
    /**
     * Obtiene información del usuario actual
     */
    public static Map<String, Object> getCurrentUser() {
        if (uid == null) return null;
        
        Object[] result = (Object[]) execute("res.users", "read", 
            Collections.singletonList(Collections.singletonList(uid)),
            Map.of("fields", Arrays.asList("name", "login", "email", "image_128"))
        );
        
        if (result != null && result.length > 0) {
            return (Map<String, Object>) result[0];
        }
        return null;
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    public static boolean isAuthenticated() {
        return uid != null && objectClient != null;
    }
    
    /**
     * Cierra la sesión actual
     */
    public static void logout() {
        uid = null;
        currentPassword = null;
        commonClient = null;
        objectClient = null;
        logger.info("Sesión de Odoo cerrada");
    }
    
    public static Integer getUid() {
        return uid;
    }
}
