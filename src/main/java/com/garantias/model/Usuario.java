package com.garantias.model;

/**
 * Modelo simple para Usuario autenticado vía Odoo
 */
public class Usuario {
    
    private Integer id;
    private String nombre;
    private String login;
    private String email;
    private String rol;
    private byte[] avatar;
    
    public Usuario() {}
    
    public Usuario(Integer id, String nombre, String login, String email) {
        this.id = id;
        this.nombre = nombre;
        this.login = login;
        this.email = email;
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public byte[] getAvatar() { return avatar; }
    public void setAvatar(byte[] avatar) { this.avatar = avatar; }
    
    public String getInitials() {
        if (nombre == null || nombre.isEmpty()) return "?";
        String[] parts = nombre.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return nombre.substring(0, Math.min(2, nombre.length())).toUpperCase();
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", login='" + login + '\'' +
                '}';
    }
}
