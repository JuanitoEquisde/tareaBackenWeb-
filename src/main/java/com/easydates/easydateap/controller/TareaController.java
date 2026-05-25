package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.TareaDTO;
import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import com.easydates.easydateap.service.ITareaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cliente/tareas")
public class TareaController {

    @Autowired
    private ITareaService tareaService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IEtiquetaService etiquetaService;

    // Guardar tarea (desde el modal)
    @PostMapping("/guardar")
    public String guardarTarea(@ModelAttribute TareaDTO dto,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            if (dto.getId() != null && dto.getId() > 0) {
                // Actualizar existente
                tareaService.actualizarTarea(dto.getId(), dto);
                redirectAttributes.addFlashAttribute("mensaje", "✅ Tarea actualizada correctamente");
            } else {
                // Crear nueva
                tareaService.crearTarea(dto, usuarioId);
                redirectAttributes.addFlashAttribute("mensaje", "✅ Tarea creada correctamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }

        return "redirect:/cliente/home";
    }

    // Eliminar tarea
    @PostMapping("/{id}/eliminar")
    public String eliminarTarea(@PathVariable Integer id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        if (tareaService.eliminarLogico(id)) {
            redirectAttributes.addFlashAttribute("mensaje", "🗑️ Tarea eliminada");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ No se pudo eliminar la tarea");
        }

        return "redirect:/cliente/tareas";
    }

    // Cambiar estado de tarea (drag & drop - AJAX)
    @PutMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id,
                                           @RequestBody Map<String, String> request) {
        String nuevoEstado = request.get("estado");

        if (tareaService.cambiarEstadoTarea(id, nuevoEstado)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado actualizado");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Error al actualizar");
        return ResponseEntity.badRequest().body(response);
    }

    // Obtener tarea por ID (para editar - AJAX)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerTarea(@PathVariable Integer id) {
        return tareaService.obtenerTarea(id)
                .map(tarea -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", tarea.getId());
                    response.put("titulo", tarea.getTitulo());
                    response.put("descripcion", tarea.getDescripcion());
                    response.put("prioridad", tarea.getPrioridad());
                    response.put("estadoTarea", tarea.getEstadoTarea());

                    if (tarea.getFechaLimite() != null) {
                        response.put("fechaLimite", tarea.getFechaLimite().toString());
                    } else {
                        response.put("fechaLimite", null);
                    }

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Buscar tareas
    @GetMapping("/buscar")
    public String buscarTareas(@RequestParam String q,
                               HttpSession session,
                               Map<String, Object> model) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        List<Tarea> resultados = tareaService.buscarPorTermino(usuarioId, q);
        model.put("tareasBusqueda", resultados);
        model.put("terminoBusqueda", q);

        return "client/resultados-busqueda";
    }

    // ✅ MOSTRAR FORMULARIO DE EDICIÓN
    @GetMapping("/{id}/editar")
    public String mostrarEditarTarea(@PathVariable Integer id,
                                     HttpSession session,
                                     Model model) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Optional<Tarea> tareaOpt = tareaService.obtenerTarea(id);
        if (tareaOpt.isPresent()) {
            Tarea tarea = tareaOpt.get();

            TareaDTO dto = new TareaDTO();
            dto.setId(tarea.getId());
            dto.setTitulo(tarea.getTitulo());
            dto.setDescripcion(tarea.getDescripcion());
            dto.setPrioridad(tarea.getPrioridad());
            dto.setEstadoTarea(tarea.getEstadoTarea());
            dto.setFechaLimite(tarea.getFechaLimite());
            dto.setCategoriaId(tarea.getCategoria() != null ? tarea.getCategoria().getId() : null);

            model.addAttribute("tarea", dto);
            model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));
            model.addAttribute("etiquetas", etiquetaService.listarTodas());
            model.addAttribute("modoEdicion", true);

            return "client/tarea-form";
        }

        return "redirect:/cliente/tareas";
    }

    // ✅ ACTUALIZAR TAREA - FORMULARIO TRADICIONAL (usa @ModelAttribute)
    @PostMapping("/{id}/actualizar")
    public String actualizarTarea(@PathVariable Integer id,
                                  @ModelAttribute TareaDTO dto,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            tareaService.actualizarTarea(id, dto);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Tarea actualizada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error: " + e.getMessage());
        }

        return "redirect:/cliente/tareas";
    }

    // ✅ ACTUALIZAR TAREA - AJAX (usa @RequestBody para JSON)
    @PostMapping("/{id}/actualizar-ajax")
    @ResponseBody
    public ResponseEntity<?> actualizarTareaAjax(@PathVariable Integer id,
                                                 @RequestBody TareaDTO dto,
                                                 HttpSession session) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }

        try {
            tareaService.actualizarTarea(id, dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tarea actualizada");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ✅ CAMBIAR ESTADO (versión POST para formularios)
    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstadoForm(@PathVariable Integer id,
                                    @RequestParam String nuevoEstado,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {

        if (tareaService.cambiarEstadoTarea(id, nuevoEstado)) {
            redirectAttributes.addFlashAttribute("mensaje", "✅ Estado actualizado");
        } else {
            redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar estado");
        }

        return "redirect:/cliente/tareas";
    }
}