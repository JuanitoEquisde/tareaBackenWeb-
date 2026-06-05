package com.easydates.easydateap.dto;

import java.time.LocalDateTime;

public class NotificacionDTO {
    private Integer id;
    private String titulo;
    private String mensaje;
    private String tipo;
    private Boolean leido;
    private String linkAccion;
    private LocalDateTime fechaCreacion;

    public NotificacionDTO() {}

    public NotificacionDTO(Integer id, String titulo, String mensaje, String tipo,
                           Boolean leido, String linkAccion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.leido = leido;
        this.linkAccion = linkAccion;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Boolean getLeido() { return leido; }
    public void setLeido(Boolean leido) { this.leido = leido; }

    public String getLinkAccion() { return linkAccion; }
    public void setLinkAccion(String linkAccion) { this.linkAccion = linkAccion; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}