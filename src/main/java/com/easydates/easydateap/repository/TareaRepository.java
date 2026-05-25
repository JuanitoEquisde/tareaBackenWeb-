package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {


    List<Tarea> findByUsuarioId(Integer usuarioId);

    List<Tarea> findByUsuarioIdAndEstado(Integer usuarioId, String estado);

    List<Tarea> findByUsuarioIdAndEstadoAndEstadoTarea(Integer usuarioId, String estado, String estadoTarea);

    List<Tarea> findByUsuarioIdAndCategoriaId(Integer usuarioId, Integer categoriaId);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO' " +
            "AND (LOWER(t.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
            "LOWER(t.descripcion) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Tarea> buscarPorTermino(@Param("usuarioId") Integer usuarioId, @Param("termino") String termino);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO'")
    Long contarTotalPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO' AND t.estadoTarea = 'PENDIENTE'")
    Long contarPendientesPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO' AND t.estadoTarea = 'TERMINADO'")
    Long contarCompletadasPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO' AND t.prioridad = 'ALTA' AND t.estadoTarea != 'TERMINADO'")
    Long contarUrgentesPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Query("SELECT t FROM Tarea t WHERE t.usuario.id = :usuarioId AND t.estado = 'ACTIVO' " +
            "AND t.fechaLimite BETWEEN :fechaInicio AND :fechaFin " +
            "ORDER BY t.fechaLimite ASC")
    List<Tarea> findByUsuarioIdAndRangoFechas(
            @Param("usuarioId") Integer usuarioId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    // =====================================================
    // 🔹 NUEVOS MÉTODOS PARA ADMINISTRADOR
    // =====================================================

    // ✅ Listar tareas por estado (sin filtrar por usuario)
    List<Tarea> findByEstado(String estado);

    // ✅ Contadores globales para estadísticas admin
    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.estado = :estado")
    long countByEstado(@Param("estado") String estado);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.estado = :estado AND t.estadoTarea = :estadoTarea")
    long countByEstadoAndEstadoTarea(@Param("estado") String estado, @Param("estadoTarea") String estadoTarea);

    @Query("SELECT COUNT(t) FROM Tarea t WHERE t.estado = :estado AND t.prioridad = :prioridad")
    long countByEstadoAndPrioridad(@Param("estado") String estado, @Param("prioridad") String prioridad);

    // ✅ Búsqueda avanzada para admin (con joins para filtrar por usuario)
    @Query("SELECT t FROM Tarea t WHERE t.estado = 'ACTIVO' " +
            "AND (:titulo IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) " +
            "AND (:prioridad IS NULL OR t.prioridad = :prioridad) " +
            "AND (:estadoTarea IS NULL OR t.estadoTarea = :estadoTarea) " +
            "AND (:nombreUsuario IS NULL OR LOWER(t.usuario.nombre) LIKE LOWER(CONCAT('%', :nombreUsuario, '%')))")
    List<Tarea> buscarTareasAdmin(
            @Param("titulo") String titulo,
            @Param("prioridad") String prioridad,
            @Param("estadoTarea") String estadoTarea,
            @Param("nombreUsuario") String nombreUsuario
    );
}