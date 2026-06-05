package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Notificacion;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.INotificacionService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/notificaciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminNotificacionController {

    @Autowired
    private INotificacionService notificacionService;

    @Autowired
    private IUsuarioService usuarioService;

    // Enviar notificación a un usuario específico
    @PostMapping("/enviar")
    @ResponseBody
    public ResponseEntity<?> enviarNotificacion(
            @RequestParam Integer usuarioId,
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(required = false) String linkAccion,
            HttpSession session) {

        try {
            // Verificar que el usuario existe
            if (!usuarioService.findById(usuarioId).isPresent()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Notificacion notificacion;
            if (linkAccion != null && !linkAccion.isEmpty()) {
                notificacion = notificacionService.enviarNotificacionConLink(usuarioId, titulo, mensaje, linkAccion);
            } else {
                notificacion = notificacionService.enviarNotificacion(usuarioId, titulo, mensaje, Notificacion.TipoNotificacion.ADMIN);
            }

            return ResponseEntity.ok("Notificación enviada correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar notificación: " + e.getMessage());
        }
    }

    // Enviar notificación masiva a todos los usuarios
    @PostMapping("/enviar-masiva")
    @ResponseBody
    public ResponseEntity<?> enviarNotificacionMasiva(
            @RequestParam String titulo,
            @RequestParam String mensaje,
            HttpSession session) {

        try {
            // Obtener todos los usuarios activos
            var usuarios = usuarioService.buscarConFiltros(null, null, null, "ACTIVO", null);

            int enviados = 0;
            for (Usuario usuario : usuarios) {
                notificacionService.enviarNotificacion(
                        usuario.getId(),
                        titulo,
                        mensaje,
                        Notificacion.TipoNotificacion.SISTEMA
                );
                enviados++;
            }

            return ResponseEntity.ok("Notificación enviada a " + enviados + " usuarios");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al enviar notificación masiva: " + e.getMessage());
        }
    }
}