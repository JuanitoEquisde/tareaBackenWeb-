package com.easydates.easydateap.service;

import com.easydates.easydateap.dto.SuscripcionDTO;
import com.easydates.easydateap.entity.Plan;
import com.easydates.easydateap.entity.Suscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ISuscripcionService {

    Page<Suscripcion> listarSuscripciones(Pageable pageable);

    List<Plan> listarPlanesActivos();

    Suscripcion crearSuscripcion(SuscripcionDTO dto, Integer usuarioId);

    Optional<Suscripcion> getSuscripcionActiva(Integer usuarioId);

    void verificarYActualizarSuscripcionesVencidas();

    Long getTotalSuscripcionesActivas();


    Page<Suscripcion> buscarConFiltrosAdmin(
            String estado,
            String plan,
            String usuario,
            String fechaInicio,
            String fechaFin,
            Pageable pageable);

    /**
     * Obtener estadísticas de suscripciones para dashboard
     */
    Map<String, Long> obtenerEstadisticasSuscripciones();

    /**
     * Cambiar estado de una suscripción (ACTIVA, CANCELADA, EXPIRADA)
     */
    boolean cambiarEstadoSuscripcion(Integer id, String nuevoEstado);

    /**
     * Eliminar lógica de suscripción
     */
    boolean eliminarSuscripcion(Integer id);

    /**
     * Obtener detalle completo de una suscripción
     */
    Map<String, Object> obtenerDetalleSuscripcion(Integer id);
    boolean editarSuscripcion(Integer id, Map<String, Object> datosActualizados);

}