package com.easydates.easydateap.service;

import com.easydates.easydateap.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface INotificacionService {

    // Crear notificación
    Notificacion crearNotificacion(Notificacion notificacion);

    // Enviar notificación a un usuario
    Notificacion enviarNotificacion(Integer usuarioId, String titulo, String mensaje, Notificacion.TipoNotificacion tipo);

    // Enviar notificación con link
    Notificacion enviarNotificacionConLink(Integer usuarioId, String titulo, String mensaje, String linkAccion);

    // Obtener notificaciones de un usuario (paginadas)
    Page<Notificacion> obtenerNotificaciones(Integer usuarioId, Pageable pageable);

    // Obtener no leídas
    List<Notificacion> obtenerNoLeidas(Integer usuarioId);

    // Contar no leídas
    Long contarNoLeidas(Integer usuarioId);

    // Marcar como leída
    void marcarComoLeida(Integer notificacionId);

    // Marcar todas como leídas
    void marcarTodasComoLeidas(Integer usuarioId);

    // Eliminar notificación
    void eliminarNotificacion(Integer notificacionId);

    // Notificaciones automáticas predefinidas
    void enviarRecordatorioPremium(Integer usuarioId, Integer diasParaVencer);
    void enviarBienvenidaPremium(Integer usuarioId);
    void enviarAlertaTareaVencida(Integer usuarioId, String tituloTarea);
}