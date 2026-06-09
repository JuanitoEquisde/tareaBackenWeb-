package com.easydates.easydateap.service;

import com.easydates.easydateap.dto.SuscripcionDTO;
import com.easydates.easydateap.model.Plan;
import com.easydates.easydateap.model.Suscripcion;
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
            Pageable pageable
    );

    Map<String, Long> obtenerEstadisticasSuscripciones();

    boolean cambiarEstadoSuscripcion(Integer id, String nuevoEstado);

    boolean eliminarSuscripcion(Integer id);

    Map<String, Object> obtenerDetalleSuscripcion(Integer id);

    boolean editarSuscripcion(Integer id, Map<String, Object> datosActualizados);

    Map<String, Object> obtenerEstadisticasReporte();

    void cancelarSuscripcionesPorUsuario(Integer usuarioId);

    void actualizarEstadoPremiumUsuario(Integer usuarioId);
}