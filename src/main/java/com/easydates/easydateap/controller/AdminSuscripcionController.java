package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Suscripcion;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ISuscripcionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/suscripciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminSuscripcionController {

    @Autowired
    private ISuscripcionService suscripcionService;

    // =====================================================
    // LISTAR SUSCRIPCIONES CON FILTROS Y PAGINACIÓN
    // =====================================================
    @GetMapping
    public String listarSuscripciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            HttpSession session) {

        // Validar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        // Preparar Pageable
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Obtener suscripciones con filtros
        Page<Suscripcion> suscripcionesPage = suscripcionService.buscarConFiltrosAdmin(
                estado, plan, usuario, fechaInicio, fechaFin, pageable);

        // Estadísticas
        Map<String, Long> stats = suscripcionService.obtenerEstadisticasSuscripciones();

        // Agregar al modelo
        model.addAttribute("suscripciones", suscripcionesPage.getContent());
        model.addAttribute("currentPage", suscripcionesPage.getNumber());
        model.addAttribute("totalPages", suscripcionesPage.getTotalPages());
        model.addAttribute("totalElements", suscripcionesPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Mantener filtros
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroPlan", plan);
        model.addAttribute("filtroUsuario", usuario);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("stats", stats);
        model.addAttribute("activePage", "suscripciones");


        return "admin/suscripciones";
    }

    // =====================================================
    // ✅ CAMBIAR ESTADO: Cancelar o Restaurar Suscripción
    // =====================================================
    @PostMapping("/{id}/cambiar-estado")
    @ResponseBody
    public Map<String, Object> cambiarEstadoSuscripcion(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            // nuevoEstado puede ser: "CANCELADA" o "ACTIVA" (para restaurar)
            boolean exito = suscripcionService.cambiarEstadoSuscripcion(id, nuevoEstado);

            if (exito) {
                response.put("success", true);

                String mensaje = "ACTIVA".equals(nuevoEstado)
                        ? "Suscripción restaurada correctamente"
                        : "Suscripción cancelada correctamente";

                response.put("message", mensaje);
                redirectAttributes.addFlashAttribute("toastMessage", mensaje);
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo actualizar el estado de la suscripción");
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Estado no válido: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // =====================================================
    // ✅ ELIMINAR SUSCRIPCIÓN (LÓGICO - Cambia a ELIMINADA)
    // =====================================================
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarSuscripcion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.eliminarSuscripcion(id);

            if (exito) {
                response.put("success", true);
                response.put("message", "Registro eliminado correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción eliminada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se encontró la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    // =====================================================
    // ✅ OBTENER DETALLE DE SUSCRIPCIÓN (Para modal Ver)
    // =====================================================
    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> obtenerDetalleSuscripcion(@PathVariable Integer id) {
        return suscripcionService.obtenerDetalleSuscripcion(id);
    }

    // =====================================================
    // ✅ ENDPOINT ADICIONAL: Restaurar suscripción (alias)
    // =====================================================
    @PostMapping("/{id}/restaurar")
    @ResponseBody
    public Map<String, Object> restaurarSuscripcion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Restaurar = cambiar estado a ACTIVA
            boolean exito = suscripcionService.cambiarEstadoSuscripcion(id, "ACTIVA");

            if (exito) {
                response.put("success", true);
                response.put("message", "Suscripción restaurada correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción restaurada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo restaurar la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }
    // =====================================================
// ✅ EDITAR SUSCRIPCIÓN
// =====================================================
    @PutMapping("/{id}/editar")
    @ResponseBody
    public Map<String, Object> editarSuscripcion(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> datosActualizados,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.editarSuscripcion(id, datosActualizados);

            if (exito) {
                response.put("success", true);
                response.put("message", "Suscripción actualizada correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción actualizada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se encontró la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }
}