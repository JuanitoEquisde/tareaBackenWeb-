package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.NotificacionDTO;
import com.easydates.easydateap.entity.Notificacion;
import com.easydates.easydateap.service.INotificacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente/notificaciones")
public class NotificacionController {

    @Autowired
    private INotificacionService notificacionService;

    @GetMapping("/api")
    @ResponseBody
    public Map<String, Object> obtenerNotificaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        System.out.println("🔔 [DEBUG] Solicitando notificaciones. usuarioId: " + usuarioId);

        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "No autorizado");
            return response;
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Notificacion> notificacionesPage = notificacionService.obtenerNotificaciones(usuarioId, pageable);

            // Convertir a DTO
            List<NotificacionDTO> notificacionesDTO = notificacionesPage.getContent().stream()
                    .map(n -> new NotificacionDTO(
                            n.getId(),
                            n.getTitulo(),
                            n.getMensaje(),
                            n.getTipo().name(),
                            n.getLeido(),
                            n.getLinkAccion(),
                            n.getFechaCreacion()
                    ))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("notificaciones", notificacionesDTO);  // ← Usar DTO en lugar de entidad
            response.put("totalPages", notificacionesPage.getTotalPages());
            response.put("totalElements", notificacionesPage.getTotalElements());
            response.put("noLeidas", notificacionService.contarNoLeidas(usuarioId));

            System.out.println("✅ [DEBUG] Notificaciones encontradas: " + notificacionesDTO.size());

        } catch (Exception e) {
            System.err.println("❌ [DEBUG] Error al obtener notificaciones: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // Contar no leídas (para badge)
    @GetMapping("/api/no-leidas/count")
    @ResponseBody
    public Map<String, Object> contarNoLeidas(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            response.put("count", 0);
            return response;
        }

        try {
            Long count = notificacionService.contarNoLeidas(usuarioId);
            response.put("count", count);
        } catch (Exception e) {
            System.err.println("❌ Error al contar notificaciones: " + e.getMessage());
            response.put("count", 0);
        }

        return response;
    }

    // Marcar como leída
    @PostMapping("/{id}/leer")
    @ResponseBody
    public ResponseEntity<?> marcarComoLeida(@PathVariable Integer id, HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }

        try {
            notificacionService.marcarComoLeida(id);
            return ResponseEntity.ok("Notificación marcada como leída");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al marcar como leída: " + e.getMessage());
        }
    }

    // Marcar todas como leídas
    @PostMapping("/leer-todas")
    @ResponseBody
    public ResponseEntity<?> marcarTodasComoLeidas(HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }

        try {
            notificacionService.marcarTodasComoLeidas(usuarioId);
            return ResponseEntity.ok("Todas las notificaciones marcadas como leídas");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al marcar todas: " + e.getMessage());
        }
    }

    // Eliminar notificación
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarNotificacion(@PathVariable Integer id, HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(401).body("No autorizado");
        }

        try {
            notificacionService.eliminarNotificacion(id);
            return ResponseEntity.ok("Notificación eliminada");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al eliminar: " + e.getMessage());
        }
    }

    // ✅ ENDPOINT DE PRUEBA
    @GetMapping("/test")
    @ResponseBody
    public Map<String, Object> test(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        response.put("usuarioId", session.getAttribute("usuarioId"));
        response.put("usuarioLogueado", session.getAttribute("usuarioLogueado"));
        response.put("tieneSession", session.getAttribute("usuarioId") != null);
        return response;
    }
}