package com.easydates.easydateapp.dto;

import java.time.LocalDate;
import java.util.List;  // ✅ Agrega este import

public class TareaDTO {

    private Integer id;
    private String titulo;
    private String descripcion;
    private String prioridad;
    private String estadoTarea;
    private LocalDate fechaLimite;
    private Integer categoriaId;

    // ✅ AGREGA ESTE CAMPO
    private List<Integer> etiquetasIds;  // Lista de IDs de etiquetas seleccionadas

    // ✅ AGREGA GETTER Y SETTER
    public List<Integer> getEtiquetasIds() {
        return etiquetasIds;
    }

    public void setEtiquetasIds(List<Integer> etiquetasIds) {
        this.etiquetasIds = etiquetasIds;
    }

    // ... resto de getters y setters existentes ...

    // Getters y setters de los otros campos
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public String getEstadoTarea() { return estadoTarea; }
    public void setEstadoTarea(String estadoTarea) { this.estadoTarea = estadoTarea; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
}