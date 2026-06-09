package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.model.Notificacion;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.repository.NotificacionRepository;
import com.easydates.easydateap.service.INotificacionService;
import com.easydates.easydateap.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class NotificacionServiceImpl implements INotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @Override
    public Notificacion crearNotificacion(Notificacion notificacion) {
        notificacion.setFechaCreacion(LocalDateTime.now());
        return notificacionRepository.save(notificacion);
    }

    @Override
    public Notificacion enviarNotificacion(Integer usuarioId, String titulo, String mensaje, Notificacion.TipoNotificacion tipo) {
        Usuario usuario = usuarioService.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion(usuario, titulo, mensaje, tipo);
        return crearNotificacion(notificacion);
    }

    @Override
    public Notificacion enviarNotificacionConLink(Integer usuarioId, String titulo, String mensaje, String linkAccion) {
        Notificacion notificacion = enviarNotificacion(usuarioId, titulo, mensaje, Notificacion.TipoNotificacion.ADMIN);
        notificacion.setLinkAccion(linkAccion);
        return notificacionRepository.save(notificacion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notificacion> obtenerNotificaciones(Integer usuarioId, Pageable pageable) {
        return notificacionRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notificacion> obtenerNoLeidas(Integer usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidoFalseOrderByFechaCreacionDesc(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarNoLeidas(Integer usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidoFalse(usuarioId);
    }

    @Override
    public void marcarComoLeida(Integer notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.marcarComoLeida();
        notificacionRepository.save(notificacion);
    }

    @Override
    public void marcarTodasComoLeidas(Integer usuarioId) {
        notificacionRepository.marcarTodasComoLeidas(usuarioId);
    }

    @Override
    public void eliminarNotificacion(Integer notificacionId) {
        notificacionRepository.deleteById(notificacionId);
    }

    // ===== NOTIFICACIONES AUTOMÁTICAS =====

    @Override
    public void enviarRecordatorioPremium(Integer usuarioId, Integer diasParaVencer) {
        String titulo = "⚠️ Tu suscripción Premium está por vencer";
        String mensaje = String.format(
                "Tu suscripción Premium vencerá en %d días. ¡Renueva ahora para no perder acceso a todas las funcionalidades!",
                diasParaVencer
        );
        enviarNotificacionConLink(usuarioId, titulo, mensaje, "/suscripciones/comprar");
    }

    @Override
    public void enviarBienvenidaPremium(Integer usuarioId) {
        String titulo = "🎉 ¡Bienvenido a Premium!";
        String mensaje = "Gracias por unirte a NotyGo Premium. Ahora tienes acceso a todas las funcionalidades avanzadas. ¡Disfruta tu experiencia!";
        enviarNotificacionConLink(usuarioId, titulo, mensaje, "/cliente/home");
    }

    @Override
    public void enviarAlertaTareaVencida(Integer usuarioId, String tituloTarea) {
        String titulo = "📅 Tarea vencida";
        String mensaje = String.format("La tarea \"%s\" ha vencido. Revísala y actualiza su estado.", tituloTarea);
        enviarNotificacionConLink(usuarioId, titulo, mensaje, "/cliente/tareas");
    }
}