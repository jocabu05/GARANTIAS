package com.garantias.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuración de conexión a MongoDB
 */
public class MongoDBConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(MongoDBConfig.class);
    
    // MongoDB de Docker con autenticación
    private static final String CONNECTION_STRING = "mongodb://admin:mongo_password_2024@localhost:27018/garantias_db?authSource=admin";
    private static final String DATABASE_NAME = "garantias_db";
    
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    
    /**
     * Obtiene la instancia de la base de datos MongoDB
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            connect();
        }
        return database;
    }
    
    /**
     * Establece la conexión con MongoDB
     */
    private static void connect() {
        try {
            logger.info("Conectando a MongoDB...");
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            logger.info("✅ Conexión a MongoDB establecida correctamente");
        } catch (Exception e) {
            logger.error("❌ Error al conectar con MongoDB: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar a MongoDB", e);
        }
    }
    
    /**
     * Cierra la conexión con MongoDB
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("Conexión a MongoDB cerrada");
        }
    }
    
    /**
     * Verifica si la conexión está activa
     */
    public static boolean isConnected() {
        try {
            if (mongoClient != null) {
                mongoClient.listDatabaseNames().first();
                return true;
            }
        } catch (Exception e) {
            logger.warn("MongoDB no está conectado: {}", e.getMessage());
        }
        return false;
    }
}
