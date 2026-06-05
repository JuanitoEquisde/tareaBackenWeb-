package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Obtener notificaciones de un usuario
    Page<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Integer usuarioId, Pageable pageable);

    // Contar no leídas
    Long countByUsuarioIdAndLeidoFalse(Integer usuarioId);

    // Obtener no leídas
    List<Notificacion> findByUsuarioIdAndLeidoFalseOrderByFechaCreacionDesc(Integer usuarioId);

    // Obtener últimas 5 notificaciones
    @Query("SELECT n FROM Notificacion n WHERE n.usuario.id = :usuarioId ORDER BY n.fechaCreacion DESC LIMIT 5")
    List<Notificacion> findUltimasNotificaciones(@Param("usuarioId") Integer usuarioId);

    // Marcar todas como leídas
    @Modifying
    @Query("UPDATE Notificacion n SET n.leido = true, n.fechaLectura = CURRENT_TIMESTAMP WHERE n.usuario.id = :usuarioId AND n.leido = false")
    void marcarTodasComoLeidas(@Param("usuarioId") Integer usuarioId);
}