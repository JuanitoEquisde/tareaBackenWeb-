package com.easydates.easydateap.dto;

import java.time.LocalDate;

public class CustomerSuccessDTO {

    private Integer usuarioId;
    private String nombre;
    private String email;
    private String planActual;
    private LocalDate fechaVencimiento;

    private Long tareasTotales;
    private Long tareasCompletadas;
    private Long tareasVencidas;

    private Integer diasSinActividad;
    private LocalDate ultimaActividad;
    private Integer healthScore;
    private String nivelRiesgo;
    private String recomendacion;
    private Boolean esPremium;
    private Boolean suscripcionActiva;

    // Constructores
    public CustomerSuccessDTO() {}

    // ✅ Constructor que coincide con el query JPQL del repository (10 parámetros)
    public CustomerSuccessDTO(Integer usuarioId, String nombre, String email, String planActual,
                              LocalDate fechaVencimiento, Long tareasTotales, Long tareasCompletadas,
                              Long tareasVencidas, Boolean esPremium, Boolean suscripcionActiva) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.email = email;
        this.planActual = planActual != null ? planActual : "Gratuito";
        this.fechaVencimiento = fechaVencimiento;

        // ✅ Evitar nulls en conteos
        this.tareasTotales = tareasTotales != null ? tareasTotales : 0L;
        this.tareasCompletadas = tareasCompletadas != null ? tareasCompletadas : 0L;
        this.tareasVencidas = tareasVencidas != null ? tareasVencidas : 0L;

        this.esPremium = esPremium != null ? esPremium : false;
        this.suscripcionActiva = suscripcionActiva != null ? suscripcionActiva : false;

        // Calcular health score y nivel de riesgo
        calcularHealthScore();
    }

    private void calcularHealthScore() {
        int score = 100;

        // ✅ Penalizar por tareas vencidas (usar longValue para evitar problemas)
        if (tareasVencidas != null && tareasVencidas > 0) {
            score -= Math.min(tareasVencidas.intValue() * 10, 30);
        }

        // Penalizar por inactividad
        if (diasSinActividad != null && diasSinActividad > 7) {
            score -= Math.min((diasSinActividad - 7) * 2, 25);
        }

        // Penalizar si la suscripción está por vencer
        if (fechaVencimiento != null) {
            long diasParaVencer = LocalDate.now().until(fechaVencimiento).getDays();
            if (diasParaVencer < 0) {
                score -= 30;
            } else if (diasParaVencer < 7) {
                score -= 15;
            }
        }

        // Bonus por completitud
        if (tareasTotales != null && tareasTotales > 0) {
            double tasaCompletitud = (double) tareasCompletadas / tareasTotales;
            if (tasaCompletitud > 0.8) {
                score += 10;
            }
        }

        this.healthScore = Math.max(0, Math.min(100, score));

        // Determinar nivel de riesgo
        if (healthScore >= 80) {
            this.nivelRiesgo = "BAJO";
            this.recomendacion = "Usuario saludable - Mantener engagement";
        } else if (healthScore >= 60) {
            this.nivelRiesgo = "MEDIO";
            this.recomendacion = "Monitorear actividad - Enviar recordatorios";
        } else if (healthScore >= 40) {
            this.nivelRiesgo = "ALTO";
            this.recomendacion = "Riesgo de churn - Contactar proactivamente";
        } else {
            this.nivelRiesgo = "CRITICO";
            this.recomendacion = "Churn inminente - Intervención inmediata requerida";
        }
    }

    // ===== Getters y Setters =====
    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPlanActual() { return planActual; }
    public void setPlanActual(String planActual) { this.planActual = planActual; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Long getTareasTotales() { return tareasTotales; }
    public void setTareasTotales(Long tareasTotales) { this.tareasTotales = tareasTotales; }

    public Long getTareasCompletadas() { return tareasCompletadas; }
    public void setTareasCompletadas(Long tareasCompletadas) { this.tareasCompletadas = tareasCompletadas; }

    public Long getTareasVencidas() { return tareasVencidas; }
    public void setTareasVencidas(Long tareasVencidas) { this.tareasVencidas = tareasVencidas; }

    public Integer getDiasSinActividad() { return diasSinActividad; }
    public void setDiasSinActividad(Integer diasSinActividad) { this.diasSinActividad = diasSinActividad; }

    public LocalDate getUltimaActividad() { return ultimaActividad; }
    public void setUltimaActividad(LocalDate ultimaActividad) { this.ultimaActividad = ultimaActividad; }

    public Integer getHealthScore() { return healthScore; }
    public void setHealthScore(Integer healthScore) { this.healthScore = healthScore; }

    public String getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(String nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }

    public String getRecomendacion() { return recomendacion; }
    public void setRecomendacion(String recomendacion) { this.recomendacion = recomendacion; }

    public Boolean getEsPremium() { return esPremium; }
    public void setEsPremium(Boolean esPremium) { this.esPremium = esPremium; }

    public Boolean getSuscripcionActiva() { return suscripcionActiva; }
    public void setSuscripcionActiva(Boolean suscripcionActiva) { this.suscripcionActiva = suscripcionActiva; }
}