package com.easydates.easydateapp.controller;

import com.easydates.easydateapp.entity.Tarea;
import com.easydates.easydateapp.entity.Usuario;
import com.easydates.easydateapp.service.ITareaService;
import com.easydates.easydateapp.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")  // ← AGREGAR: Base path para todas las rutas
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    @Autowired
    private IUsuarioService usuarioService;
    @Autowired
    private ITareaService tareaService;

    // ✅ Obtener usuario por ID
    @GetMapping("/api/usuarios/{id}")  // → /admin/api/usuarios/{id}
    @ResponseBody
    public ResponseEntity<?> obtenerUsuario(@PathVariable Integer id) {
        System.out.println("🔍 API: Obteniendo usuario con ID: " + id);

        try {
            return usuarioService.findById(id)
                    .map(usuario -> {
                        System.out.println("✅ Usuario encontrado: " + usuario.getNombre());

                        Map<String, Object> response = new HashMap<>();
                        response.put("id", usuario.getId());
                        response.put("nombre", usuario.getNombre());
                        response.put("email", usuario.getEmail());
                        response.put("rolId", usuario.getRol() != null ? usuario.getRol().getId() : null);
                        response.put("estado", usuario.getEstado());

                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        System.err.println("❌ Usuario no encontrado con ID: " + id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            System.err.println("💥 Error al obtener usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // ✅ Actualizar usuario
    @PutMapping("/api/usuarios/{id}/actualizar")  // → /admin/api/usuarios/{id}/actualizar
    @ResponseBody
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Usuario usuarioActualizado) {

        System.out.println("🔧 API: Actualizando usuario ID: " + id);
        System.out.println("📥 Datos recibidos: " + usuarioActualizado);

        try {
            Usuario resultado = usuarioService.actualizar(id, usuarioActualizado);
            System.out.println("✅ Usuario actualizado correctamente");
            return ResponseEntity.ok("Usuario actualizado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar: " + e.getMessage());
            e.printStackTrace();  // ← AGREGAR: Para ver el error completo
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }

    // ✅ Cambiar estado de usuario
    @PostMapping("/api/usuarios/{id}/cambiar-estado")  // → /admin/api/usuarios/{id}/cambiar-estado
    @ResponseBody
    public Map<String, Object> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam String estado) {

        System.out.println("🔄 API: Cambiando estado usuario ID: " + id + " a: " + estado);

        Map<String, Object> response = new HashMap<>();
        boolean exito = usuarioService.cambiarEstado(id, estado);

        response.put("success", exito);
        response.put("message", exito ? "Estado actualizado" : "Error al actualizar");

        return response;
    }

    // =====================================================
    // 🔹 ENDPOINTS VISTAS (Retornan HTML)
    // Rutas: /admin/...
    // =====================================================

    @GetMapping("/dashboard")  // → /admin/dashboard
    public String dashboard(Model model, HttpSession session) {
        System.out.println("📊 Accediendo a vista: /admin/dashboard");

        Usuario admin = (Usuario) session.getAttribute("usuario");
        System.out.println("👤 Usuario en sesión: " + (admin != null ? admin.getNombre() : "NULL"));

        if (admin == null || !admin.isAdmin()) {
            System.err.println("❌ Usuario no es admin o no está en sesión");
            return "redirect:/cliente/home";
        }

        System.out.println("✅ Usuario es admin, cargando dashboard...");

        Map<String, Object> stats = usuarioService.obtenerEstadisticas();
        model.addAttribute("stats", stats);

        List<Usuario> recientes = usuarioService.buscarConFiltros(null, null, null, null)
                .stream()
                .limit(5)
                .toList();
        model.addAttribute("usuariosRecientes", recientes);

        return "admin/dashboard";
    }

    @GetMapping("/usuarios")  // → /admin/usuarios
    public String listarUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rol,
            Model model,
            HttpSession session) {

        System.out.println("📋 Accediendo a vista: /admin/usuarios");

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        List<Usuario> usuarios = usuarioService.buscarConFiltros(nombre, email, estado, rol);
        model.addAttribute("usuarios", usuarios != null ? usuarios : List.of());
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroEmail", email);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroRol", rol);

        return "admin/usuarios";
    }

    //  Listar tareas - Vista HTML (para admin)
    @GetMapping("/tareas")
    public String listarTareas(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            Model model,
            HttpSession session) {

        System.out.println("📋 Accediendo a vista: /admin/tareas");

        // Verificar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        // Buscar tareas con filtros de admin
        List<Tarea> tareas = tareaService.buscarTareasAdmin(titulo, prioridad, estado, usuario);

        // ✅ Calcular estadísticas en Java (NO en Thymeleaf)
        long total = tareas != null ? tareas.size() : 0;
        long pendientes = tareas != null ? tareas.stream().filter(t -> "PENDIENTE".equals(t.getEstadoTarea())).count() : 0;
        long enProgreso = tareas != null ? tareas.stream().filter(t -> "EN_PROGRESO".equals(t.getEstadoTarea())).count() : 0;
        long terminadas = tareas != null ? tareas.stream().filter(t -> "TERMINADO".equals(t.getEstadoTarea())).count() : 0;

        // Agregar al modelo
        model.addAttribute("totalTareas", total);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enProgreso", enProgreso);
        model.addAttribute("terminadas", terminadas);

        model.addAttribute("tareas", tareas != null ? tareas : List.of());
        model.addAttribute("filtroTitulo", titulo);
        model.addAttribute("filtroPrioridad", prioridad);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroUsuario", usuario);

        return "admin/tareas";
    }

}
