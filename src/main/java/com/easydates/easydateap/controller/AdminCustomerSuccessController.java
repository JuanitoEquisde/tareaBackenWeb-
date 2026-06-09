package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.CustomerSuccessDTO;
import com.easydates.easydateap.model.Notificacion;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.service.ICustomerSuccessService;
import com.easydates.easydateap.service.INotificacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/customer-success")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminCustomerSuccessController {

    @Autowired
    private ICustomerSuccessService customerSuccessService;

    @Autowired
    private INotificacionService notificacionService; // ✅ AGREGAR ESTA INYECCIÓN

    @GetMapping
    public String customerSuccessDashboard(
            @RequestParam(required = false) String filtro,
            Model model,
            HttpSession session) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        Map<String, Object> metricas = customerSuccessService.obtenerMetricasGenerales();
        model.addAttribute("metricas", metricas);

        List<CustomerSuccessDTO> usuarios;
        if (filtro == null || filtro.isEmpty() || "todos".equals(filtro)) {
            usuarios = customerSuccessService.obtenerTodosLosUsuariosConHealthScore();
        } else if ("riesgo".equals(filtro)) {
            usuarios = customerSuccessService.obtenerUsuariosEnRiesgo("ALTO")
                    .stream().limit(20).toList();
        } else if ("upsell".equals(filtro)) {
            usuarios = customerSuccessService.obtenerOportunidadesUpsell()
                    .stream().limit(20).toList();
        } else {
            usuarios = customerSuccessService.obtenerTodosLosUsuariosConHealthScore();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("filtroActual", filtro != null ? filtro : "todos");
        model.addAttribute("activePage", "customer-success");

        return "admin/customer-success";
    }

    @GetMapping("/{id}/detalle")
    @ResponseBody
    public Map<String, Object> obtenerDetalleUsuario(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        CustomerSuccessDTO usuario = customerSuccessService.obtenerDetalleUsuario(id);
        if (usuario != null) {
            response.put("success", true);
            response.put("usuario", usuario);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
        }
        return response;
    }

    // ✅ NUEVO ENDPOINT: Enviar mensaje/notificación a un usuario
    @PostMapping("/enviar-mensaje")
    @ResponseBody
    public ResponseEntity<?> enviarMensaje(
            @RequestParam Integer usuarioId,
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(required = false) String linkAccion,
            HttpSession session) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return ResponseEntity.status(401).body("No autorizado");
        }

        try {
            // Validar que el usuarioId sea válido
            if (usuarioId == null || usuarioId <= 0) {
                return ResponseEntity.badRequest().body("ID de usuario inválido");
            }

            // Validar campos obligatorios
            if (titulo == null || titulo.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El título es obligatorio");
            }
            if (mensaje == null || mensaje.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El mensaje es obligatorio");
            }

            // ✅ Enviar la notificación al usuario
            Notificacion notificacion;
            if (linkAccion != null && !linkAccion.trim().isEmpty()) {
                notificacion = notificacionService.enviarNotificacionConLink(
                        usuarioId, titulo.trim(), mensaje.trim(), linkAccion.trim());
            } else {
                notificacion = notificacionService.enviarNotificacion(
                        usuarioId, titulo.trim(), mensaje.trim(), Notificacion.TipoNotificacion.ADMIN);
            }

            System.out.println("✅ [ADMIN] Mensaje enviado al usuario ID: " + usuarioId);
            System.out.println("   📧 Título: " + titulo);
            System.out.println("   📝 Notificación ID: " + notificacion.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mensaje enviado correctamente");
            response.put("notificacionId", notificacion.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ [ADMIN] Error al enviar mensaje: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al enviar mensaje: " + e.getMessage());
        }
    }
}