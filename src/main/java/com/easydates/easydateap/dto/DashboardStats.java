package com.easydates.easydateap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para estadísticas del dashboard administrativo
 */
@Data
@NoArgsConstructor
public class DashboardStats {

    private long totalUsuarios;
    private long usuariosActivos;
    private long usuariosInactivos;
    private long totalAdmins;

    // Constructor con valores por defecto
    public DashboardStats(long total, long activos, long inactivos, long admins) {
        this.totalUsuarios = total;
        this.usuariosActivos = activos;
        this.usuariosInactivos = inactivos;
        this.totalAdmins = admins;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(long usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
    }

    public long getUsuariosInactivos() {
        return usuariosInactivos;
    }

    public void setUsuariosInactivos(long usuariosInactivos) {
        this.usuariosInactivos = usuariosInactivos;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }
}