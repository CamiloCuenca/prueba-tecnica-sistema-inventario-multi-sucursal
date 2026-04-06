package com.camilocuenca.inventorysystem.dto;

import java.time.LocalDateTime;

public class AuditLogDto {
    private Long id;
    private String accion;
    private String usuario;
    private String descripcion;
    private String resultado;
    private String errorMensaje;
    private LocalDateTime fecha;

    public AuditLogDto() {}

    public AuditLogDto(Long id, String accion, String usuario, String descripcion, String resultado, String errorMensaje, LocalDateTime fecha) {
        this.id = id;
        this.accion = accion;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.resultado = resultado;
        this.errorMensaje = errorMensaje;
        this.fecha = fecha;
    }

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getErrorMensaje() { return errorMensaje; }
    public void setErrorMensaje(String errorMensaje) { this.errorMensaje = errorMensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}

