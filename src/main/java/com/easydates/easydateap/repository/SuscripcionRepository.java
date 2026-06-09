package com.easydates.easydateap.repository;

import com.easydates.easydateap.model.Suscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {


    List<Suscripcion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    @Query("SELECT s FROM Suscripcion s WHERE s.usuario.id = :usuarioId AND s.estado = :estado")
    Optional<Suscripcion> findSuscripcionActivaByUsuarioId(@Param("usuarioId") Integer usuarioId,
                                                           @Param("estado") Suscripcion.EstadoSuscripcion estado);
    List<Suscripcion> findByUsuarioIdAndEstado(Integer usuarioId, Suscripcion.EstadoSuscripcion estado);
    @Query("SELECT s FROM Suscripcion s WHERE s.fechaFin < :fechaHoy AND s.estado = :estado")
    List<Suscripcion> findSuscripcionesVencidas(@Param("fechaHoy") LocalDate fechaHoy,
                                                @Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = :estado")
    Long countSuscripcionesActivas(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s JOIN FETCH s.usuario JOIN FETCH s.plan ORDER BY s.fechaCreacion DESC")
    Page<Suscripcion> findAllWithDetails(Pageable pageable);

    // =====================================================
    // ✅ NUEVO: Eliminar suscripciones por usuario ID
    // =====================================================
    @Modifying
    @Transactional
    @Query("DELETE FROM Suscripcion s WHERE s.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // =====================================================
    // ✅ MÉTODO PARA BUSCAR CON FILTROS (CORREGIDO)
    // =====================================================
    @Query("SELECT s FROM Suscripcion s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH s.plan p " +
            "WHERE (:estado IS NULL OR s.estado = :estado) " +
            "AND (:plan IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :plan, '%'))) " +
            "AND (:usuario IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :usuario, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :usuario, '%'))) " +
            "AND (:fechaInicio IS NULL OR s.fechaInicio >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR s.fechaFin <= :fechaFin) " +
            "AND s.estado <> 'ELIMINADA' " +
            "ORDER BY s.fechaCreacion DESC")
    Page<Suscripcion> buscarConFiltrosAdmin(
            @Param("estado") Suscripcion.EstadoSuscripcion estado,
            @Param("plan") String plan,
            @Param("usuario") String usuario,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            Pageable pageable
    );
    @Query("SELECT s FROM Suscripcion s " +
            "JOIN FETCH s.usuario u " +
            "JOIN FETCH s.plan p " +
            "WHERE (:estado IS NULL OR s.estado = :estado) " +
            "AND (:plan IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :plan, '%'))) " +
            "AND (:usuario IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :usuario, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :usuario, '%'))) " +
            "AND (:fechaInicio IS NULL OR s.fechaInicio >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR s.fechaFin <= :fechaFin) " +
            "AND s.estado <> 'ELIMINADA' " +
            "ORDER BY s.fechaCreacion DESC")
    List<Suscripcion> buscarSinPaginado(
            @Param("estado") Suscripcion.EstadoSuscripcion estado,
            @Param("plan") String plan,
            @Param("usuario") String usuario,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );




    // =====================================================
    // ✅ MÉTODOS DE ESTADÍSTICAS
    // =====================================================
    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = :estado")
    Long countByEstado(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COALESCE(SUM(s.precioPagado), 0) FROM Suscripcion s WHERE s.estado = :estado")
    BigDecimal sumarPreciosPagadosByEstado(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.usuario.id = :usuarioId AND s.estado = :estado")
    Long countByUsuarioIdAndEstado(@Param("usuarioId") Integer usuarioId,
                                   @Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COALESCE(SUM(s.precioPagado), 0) FROM Suscripcion s WHERE s.estado = 'ACTIVA'")
    BigDecimal sumarPreciosPagados();

    @Query("SELECT p.nombre, COUNT(s) FROM Suscripcion s JOIN s.plan p GROUP BY p.nombre")
    List<Object[]> contarPorPlan();

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado <> 'ELIMINADA'")
    Long countActivasNoEliminadas();


}