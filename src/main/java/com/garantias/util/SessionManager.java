package com.garantias.util;

import com.garantias.model.Usuario;

/**
 * Singleton para gestionar la sesión del usuario actual
 */
public class SessionManager {
    
    private static SessionManager instance;
    private Usuario currentUser;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void setCurrentUser(Usuario user) {
        this.currentUser = user;
    }
    
    public Usuario getCurrentUser() {
        return currentUser;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public void logout() {
        this.currentUser = null;
    }
    
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNombre() : "Usuario";
    }
}
