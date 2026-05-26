package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.HistorialCambios;
import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ITareaService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
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


    @GetMapping("/dashboard")
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

        List<Usuario> recientes = usuarioService.buscarConFiltrosOr(null, null, null, null)
                .stream()
                .limit(5)
                .toList();
        model.addAttribute("usuariosRecientes", recientes);

        return "admin/dashboard";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model,
            HttpSession session) {

        int paginaActual = (page != null && page > 0) ? page : 1;
        int tamanoPagina = (size != null && size > 0) ? size : 10;

        List<Usuario> todosUsuarios = usuarioService.buscarConFiltrosOr(nombre, email, estado, null);

        int totalElementos = todosUsuarios.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);

        // Calcular índices para paginación
        int startIndex = (paginaActual - 1) * tamanoPagina;
        int endIndex = Math.min(startIndex + tamanoPagina, totalElementos);

        List<Usuario> usuariosPaginados = todosUsuarios.subList(startIndex, endIndex);

        model.addAttribute("usuarios", usuariosPaginados);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("tamanoPagina", tamanoPagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalElementos", totalElementos);

        // Filtros
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroEmail", email);
        model.addAttribute("filtroEstado", estado);

        return "admin/usuarios";
    }
    // Listar tareas con PAGINACIÓN estilo Google
    @GetMapping("/tareas")
    public String listarTareas(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model,
            HttpSession session) {

        System.out.println("📋 Accediendo a vista: /admin/tareas");

        // Verificar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        // Configurar paginación
        int paginaActual = (page != null && page > 0) ? page : 1;
        int tamanoPagina = (size != null && size > 0) ? size : 10;

        // Buscar tareas con filtros
        List<Tarea> todasLasTareas = tareaService.buscarTareasAdmin(titulo, prioridad, estado, usuario);

        // Calcular totales para paginación
        int totalElementos = todasLasTareas.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);

        // Calcular índices para paginación manual
        int startIndex = (paginaActual - 1) * tamanoPagina;
        int endIndex = Math.min(startIndex + tamanoPagina, totalElementos);

        // Obtener solo la página actual
        List<Tarea> tareasPaginadas = (startIndex < totalElementos)
                ? todasLasTareas.subList(startIndex, endIndex)
                : Collections.emptyList();

        // Calcular estadísticas para las cards (sobre TODAS las tareas, no solo la página)
        long total = todasLasTareas.size();
        long pendientes = todasLasTareas.stream().filter(t -> "PENDIENTE".equals(t.getEstadoTarea())).count();
        long enProgreso = todasLasTareas.stream().filter(t -> "EN_PROGRESO".equals(t.getEstadoTarea())).count();
        long terminadas = todasLasTareas.stream().filter(t -> "TERMINADO".equals(t.getEstadoTarea())).count();

        // Agregar al modelo
        model.addAttribute("tareas", tareasPaginadas);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("tamanoPagina", tamanoPagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalElementos", totalElementos);

        model.addAttribute("totalTareas", total);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enProgreso", enProgreso);
        model.addAttribute("terminadas", terminadas);

        model.addAttribute("filtroTitulo", titulo);
        model.addAttribute("filtroPrioridad", prioridad);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroUsuario", usuario);

        return "admin/tareas";
    }
    // ✅ NUEVO: Eliminar usuario (soft delete) con auditoría
    @DeleteMapping("/api/usuarios/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(
            @PathVariable Integer id,
            HttpSession session) {

        System.out.println("🗑️ API: Eliminando usuario ID: " + id);

        try {
            // 1. Obtener usuario para auditoría
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. No permitir eliminar al admin actual
            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }

            // 3. Capturar datos antes de eliminar
            String datosUsuario = String.format(
                    "ID: %d, Nombre: %s, Email: %s, Rol: %s",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A"
            );

            // 4. Ejecutar eliminación (soft delete)
            boolean exito = usuarioService.eliminarLogico(id);

            if (exito) {
                // 5. ✅ Registrar en auditoría
                String nombreAdmin = (String) session.getAttribute("usuarioLogueado");

                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR");
                historial.setDescripcion("Admin eliminó usuario (soft delete). Datos: " + datosUsuario);
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);
                historial.setFechaCambio(LocalDateTime.now());

                return ResponseEntity.ok("Usuario eliminado correctamente");
            } else {
                return ResponseEntity.badRequest().body("Error al eliminar usuario");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    // ✅ ELIMINACIÓN PERMANENTE (Hard Delete)
    @DeleteMapping("/api/usuarios/{id}/eliminar-permanente")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioPermanente(
            @PathVariable Integer id,
            HttpSession session) {

        System.out.println("💀 API: Eliminación PERMANENTE de usuario ID: " + id);

        try {
            // 1. Obtener usuario para auditoría ANTES de eliminar
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // 2. No permitir eliminar al admin actual
            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }


            // 4. ✅ ELIMINAR PERMANENTEMENTE DE LA BD
            boolean exito = usuarioService.eliminarLogico(id);

            if (exito) {
                // 5. Registrar en auditoría (esto queda aunque el usuario ya no exista)
                String nombreAdmin = (String) session.getAttribute("usuarioLogueado");

                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR_PERMANENTE");
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);  // Se guarda la referencia antes de eliminar
                historial.setFechaCambio(LocalDateTime.now());


                return ResponseEntity.ok("Usuario eliminado permanentemente");
            } else {
                return ResponseEntity.badRequest().body("Error al eliminar usuario permanentemente");
            }

        } catch (Exception e) {
            System.err.println("❌ Error en eliminación permanente: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

}
