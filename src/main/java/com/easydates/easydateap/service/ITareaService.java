package com.easydates.easydateap.service;

import com.easydates.easydateap.dto.TareaDTO;
import com.easydates.easydateap.model.Tarea;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ITareaService {

    // =====================================================
    // 🔹 MÉTODOS EXISTENTES (NO MODIFICAR - Ya funcionan)
    // =====================================================

    Tarea crearTarea(TareaDTO dto, Integer usuarioId);

    Optional<Tarea> obtenerTarea(Integer id);

    List<Tarea> listarTareasPorUsuario(Integer usuarioId);

    List<Tarea> listarPorEstado(Integer usuarioId, String estadoTarea);

    Tarea actualizarTarea(Integer id, TareaDTO dto);

    boolean eliminarTarea(Integer id);

    boolean eliminarLogico(Integer id);

    boolean cambiarEstadoTarea(Integer id, String nuevoEstado);

    List<Tarea> buscarPorTermino(Integer usuarioId, String termino);

    List<Tarea> listarPorCategoria(Integer usuarioId, Integer categoriaId);

    long contarTotal(Integer usuarioId);

    long contarPendientes(Integer usuarioId);

    long contarCompletadas(Integer usuarioId);

    long contarUrgentes(Integer usuarioId);

    // ✅ Nuevos métodos para el calendario
    List<Tarea> listarPorFecha(Integer usuarioId, LocalDate fecha);

    List<Tarea> listarPorRangoFechas(Integer usuarioId, LocalDate fechaInicio, LocalDate fechaFin);

    List<Tarea> listarConFiltros(Integer usuarioId, String estado, String prioridad, Integer categoriaId, String busqueda, String sortBy, String sortDir);

    // =====================================================
    // 🔹 NUEVOS MÉTODOS PARA ADMINISTRADOR
    // =====================================================

    //  Listar TODAS las tareas del sistema (sin filtrar por usuario)
    List<Tarea> listarTodasLasTareas();

    // Buscar tareas con filtros globales (para panel admin)
    List<Tarea> buscarTareasAdmin(String titulo, String prioridad, String estado, String nombreUsuario);

    // Estadísticas globales para dashboard admin
    long contarTareasTotales();
    long contarTareasPorEstado(String estado);
    long contarTareasPorPrioridad(String prioridad);

    // Obtener tarea con relaciones cargadas (para edición admin)
    Optional<Tarea> obtenerTareaConDetalles(Integer id);

    // Eliminar tarea permanentemente (solo admin)
    boolean eliminarPermanente(Integer id);
    Page<Tarea> buscarTareasPaginadas(
            String titulo,
            String prioridad,
            String estado,
            String usuario,
            Pageable pageable
    );
    Page<Tarea> buscarTareasClientePaginadas(
            Integer usuarioId,
            String buscar,
            String prioridad,
            String estadoTarea,
            Integer categoriaId,
            Pageable pageable
    );
}