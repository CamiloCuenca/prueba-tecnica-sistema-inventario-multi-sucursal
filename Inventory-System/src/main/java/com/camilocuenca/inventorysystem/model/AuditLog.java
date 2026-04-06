package com.camilocuenca.inventorysystem.model;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un registro de auditoría.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "accion", nullable = false)
    private String accion;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "resultado", nullable = false)
    private String resultado;

    @Column(name = "error_mensaje", length = 2000)
    private String errorMensaje;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    public AuditLog() {}

    public AuditLog(String accion, String usuario, String descripcion, String resultado, String errorMensaje) {
        this.accion = accion;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.resultado = resultado;
        this.errorMensaje = errorMensaje;
    }

    // Getters y setters
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

