package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Suscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {

    // ✅ MÉTODOS EXISTENTES
    Optional<Suscripcion> findByUsuarioIdAndEstado(Integer usuarioId, Suscripcion.EstadoSuscripcion estado);

    List<Suscripcion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId);

    @Query("SELECT s FROM Suscripcion s WHERE s.usuario.id = :usuarioId AND s.estado = :estado")
    Optional<Suscripcion> findSuscripcionActivaByUsuarioId(@Param("usuarioId") Integer usuarioId,
                                                           @Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s WHERE s.fechaFin < :fechaHoy AND s.estado = :estado")
    List<Suscripcion> findSuscripcionesVencidas(@Param("fechaHoy") LocalDate fechaHoy,
                                                @Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = :estado")
    Long countSuscripcionesActivas(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s JOIN FETCH s.usuario JOIN FETCH s.plan ORDER BY s.fechaCreacion DESC")
    Page<Suscripcion> findAllWithDetails(Pageable pageable);


    // =====================================================
    // ✅ NUEVOS MÉTODOS PARA PANEL ADMIN (CORREGIDOS)
    // =====================================================

    @Query("SELECT s FROM Suscripcion s " +
            "JOIN s.usuario u " +
            "JOIN s.plan p " +
            "WHERE (:estado IS NULL OR s.estado = :estado) " +
            "AND (:plan IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :plan, '%'))) " +

            "AND (:usuario IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :usuario, '%')) " +
            "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :usuario, '%'))) " +
            "AND (:fechaInicio IS NULL OR s.fechaInicio >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR s.fechaInicio <= :fechaFin) " +
            "AND s.estado <> 'ELIMINADA' " +
            "ORDER BY s.fechaCreacion DESC")
    Page<Suscripcion> buscarConFiltrosAdmin(
            @Param("estado") Suscripcion.EstadoSuscripcion estado,  // ← ENUM
            @Param("plan") String plan,
            @Param("usuario") String usuario,
            @Param("fechaInicio") String fechaInicio,  // ← String para fecha
            @Param("fechaFin") String fechaFin,
            Pageable pageable);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado = :estado")
    Long countByEstado(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COALESCE(SUM(s.precioPagado), 0) FROM Suscripcion s WHERE s.estado = :estado")
    Double sumarPreciosPagadosByEstado(@Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.usuario.id = :usuarioId AND s.estado = :estado")
    Long countByUsuarioIdAndEstado(@Param("usuarioId") Integer usuarioId,
                                   @Param("estado") Suscripcion.EstadoSuscripcion estado);

    @Query("SELECT s FROM Suscripcion s JOIN s.usuario u WHERE u.email LIKE %:email%")
    List<Suscripcion> findByUsuarioEmail(@Param("email") String email);

    @Query("SELECT COUNT(s) FROM Suscripcion s WHERE s.estado <> 'ELIMINADA'")  // ✅ CORREGIDO
    Long countActivasNoEliminadas();
}