package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.DashboardStats;
import com.easydates.easydateap.entity.HistorialCambios;
import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.IHistorialCambiosService;
import com.easydates.easydateap.service.ITareaService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ITareaService tareaService;

    @Autowired
    private IHistorialCambiosService historialService;

    // =====================================================
    // API: Obtener usuario por ID
    // =====================================================
    @GetMapping("/api/usuarios/{id}")
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

    // =====================================================
    // API: Actualizar usuario
    // =====================================================
    @PutMapping("/api/usuarios/{id}/actualizar")
    @ResponseBody
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> usuarioActualizadoMap) {

        System.out.println("🔧 API: Actualizando usuario ID: " + id);
        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            if (usuarioActualizadoMap.get("nombre") != null) {
                String nombre = (String) usuarioActualizadoMap.get("nombre");
                if (!nombre.trim().isEmpty()) usuario.setNombre(nombre.trim());
            }
            if (usuarioActualizadoMap.get("email") != null) {
                String email = (String) usuarioActualizadoMap.get("email");
                if (!email.trim().isEmpty()) usuario.setEmail(email.trim());
            }
            if (usuarioActualizadoMap.get("estado") != null) {
                String estado = (String) usuarioActualizadoMap.get("estado");
                if (!estado.trim().isEmpty()) usuario.setEstado(estado.trim());
            }
            if (usuarioActualizadoMap.get("password") != null) {
                String newPassword = (String) usuarioActualizadoMap.get("password");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    usuario.setPassword(newPassword);
                }
            }

            Object rolData = usuarioActualizadoMap.get("rol");
            Integer rolId = null;
            if (rolData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rolMap = (Map<String, Object>) rolData;
                rolId = (Integer) rolMap.get("id");
            } else {
                rolId = (Integer) usuarioActualizadoMap.get("rolId");
                if (rolId == null) rolId = (Integer) usuarioActualizadoMap.get("rol");
            }
            if (rolId != null && rolId > 0) {
                usuarioService.asignarRol(id, rolId);
            }

            usuarioService.actualizar(id, usuario);
            System.out.println("✅ Usuario actualizado correctamente");
            return ResponseEntity.ok("Usuario actualizado correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }

    @PostMapping("/api/usuarios/{id}/cambiar-estado")
    @ResponseBody
    public Map<String, Object> cambiarEstado(@PathVariable Integer id, @RequestParam String estado) {
        Map<String, Object> response = new HashMap<>();
        boolean exito = usuarioService.cambiarEstado(id, estado);
        response.put("success", exito);
        response.put("message", exito ? "Estado actualizado" : "Error al actualizar");
        return response;
    }

    // =====================================================
    // ✅ VISTA: Dashboard con Estadísticas Funcionales (DTO)
    // =====================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {

        // Validar que sea administrador
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        // ✅ OBTENER ESTADÍSTICAS USANDO EL NUEVO DTO
        DashboardStats stats = usuarioService.obtenerDashboardStats();

        // OBTENER USUARIOS RECIENTES (manteniendo tu lógica existente)
        List<Usuario> usuariosRecientes = usuarioService.buscarConFiltrosOr(null, null, null, null)
                .stream()
                .limit(5)
                .toList();

        // PASAR DATOS A LA VISTA
        model.addAttribute("stats", stats);
        model.addAttribute("usuariosRecientes", usuariosRecientes);
        model.addAttribute("activePage", "dashboard");

        return "admin/dashboard";
    }

    // =====================================================
    //  VISTA: Listar usuarios con PAGINACIÓN DEL SERVIDOR
    // =====================================================
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model,
            HttpSession session) {

        // Normalizar filtros
        Integer idBusqueda = (id != null && id > 0) ? id : null;
        String nombreBusqueda = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;
        String emailBusqueda = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        String estadoBusqueda = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;

        // Crear Pageable con ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Obtener página del servicio (paginación del servidor)
        Page<Usuario> paginaUsuarios = usuarioService.buscarUsuariosPaginados(
                idBusqueda, nombreBusqueda, emailBusqueda, estadoBusqueda, pageable
        );

        // Pasar datos a la vista
        model.addAttribute("usuariosPage", paginaUsuarios);
        model.addAttribute("usuarios", paginaUsuarios.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaUsuarios.getTotalPages());
        model.addAttribute("totalElements", paginaUsuarios.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("filtroId", idBusqueda);
        model.addAttribute("filtroNombre", nombreBusqueda);
        model.addAttribute("filtroEmail", emailBusqueda);
        model.addAttribute("filtroEstado", estadoBusqueda);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("activePage", "usuarios");

        return "admin/usuarios";
    }

    // =====================================================
    // VISTA: Listar tareas con PAGINACIÓN DEL SERVIDOR
    // =====================================================
    @GetMapping("/tareas")
    public String listarTareas(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            HttpSession session) {

        // Validar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        // Normalizar filtros
        String tituloBusqueda = (titulo != null && !titulo.trim().isEmpty()) ? titulo.trim() : null;
        String prioridadBusqueda = (prioridad != null && !prioridad.trim().isEmpty()) ? prioridad.trim() : null;
        String estadoBusqueda = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        String usuarioBusqueda = (usuario != null && !usuario.trim().isEmpty()) ? usuario.trim() : null;

        // Crear Pageable con ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Tarea> paginaTareas = tareaService.buscarTareasPaginadas(
                tituloBusqueda, prioridadBusqueda, estadoBusqueda, usuarioBusqueda, pageable
        );

        // Calcular estadísticas para los cards
        long total = paginaTareas.getTotalElements();
        long pendientes = paginaTareas.getContent().stream().filter(t -> "PENDIENTE".equals(t.getEstadoTarea())).count();
        long enProgreso = paginaTareas.getContent().stream().filter(t -> "EN_PROGRESO".equals(t.getEstadoTarea())).count();
        long terminadas = paginaTareas.getContent().stream().filter(t -> "TERMINADO".equals(t.getEstadoTarea())).count();

        // Pasar datos a la vista
        model.addAttribute("tareasPage", paginaTareas);
        model.addAttribute("tareas", paginaTareas.getContent());  // Lista actual para la tabla
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaTareas.getTotalPages());
        model.addAttribute("totalElements", paginaTareas.getTotalElements());
        model.addAttribute("pageSize", size);

        // Stats para los cards
        model.addAttribute("totalTareas", total);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enProgreso", enProgreso);
        model.addAttribute("terminadas", terminadas);

        // Filtros para mantener en el formulario
        model.addAttribute("filtroTitulo", tituloBusqueda);
        model.addAttribute("filtroPrioridad", prioridadBusqueda);
        model.addAttribute("filtroEstado", estadoBusqueda);
        model.addAttribute("filtroUsuario", usuarioBusqueda);

        // Para ordenamiento
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("activePage", "tareas");

        return "admin/tareas";
    }

    // =====================================================
    // API: Eliminar usuario (mantener como está)
    // =====================================================
    @DeleteMapping("/api/usuarios/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable Integer id, HttpSession session) {
        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }
            boolean exito = usuarioService.eliminarLogico(id);
            if (exito) {
                String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR");
                historial.setDescripcion("Admin cambió estado a INACTIVO");
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);
                historial.setFechaCambio(LocalDateTime.now());
                historialService.save(historial);
                return ResponseEntity.ok("Usuario marcado como inactivo");
            }
            return ResponseEntity.badRequest().body("Error al cambiar estado del usuario");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/usuarios/{id}/eliminar-sistema")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioDelSistema(@PathVariable Integer id, HttpSession session) {
        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }
            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            boolean exito = usuarioService.eliminarDelSistema(id, nombreAdmin);
            if (exito) {
                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR_SISTEMA");
                historial.setDescripcion("Admin eliminó usuario del sistema");
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);
                historial.setFechaCambio(LocalDateTime.now());
                historialService.save(historial);
                return ResponseEntity.ok("Usuario eliminado del sistema correctamente");
            }
            return ResponseEntity.badRequest().body("Error al eliminar usuario del sistema");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/api/usuarios/{id}/restaurar")
    @ResponseBody
    public ResponseEntity<?> restaurarUsuario(@PathVariable Integer id, HttpSession session) {
        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            boolean exito = usuarioService.restaurarUsuario(id);
            if (exito) {
                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("RESTAURAR");
                historial.setDescripcion("Admin restauró usuario: " + usuario.getNombre());
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);
                historial.setFechaCambio(LocalDateTime.now());
                historialService.save(historial);
                return ResponseEntity.ok("Usuario restaurado correctamente");
            }
            return ResponseEntity.badRequest().body("El usuario no estaba eliminado o no se pudo restaurar");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/api/usuarios/crear")
    @ResponseBody
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, Object> nuevoUsuarioMap, HttpSession session) {
        try {
            String nombre = (String) nuevoUsuarioMap.get("nombre");
            String email = (String) nuevoUsuarioMap.get("email");
            String password = (String) nuevoUsuarioMap.get("password");
            String estado = (String) nuevoUsuarioMap.get("estado");

            if (usuarioService.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("El correo electrónico ya está registrado");
            }
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
            }

            Object rolData = nuevoUsuarioMap.get("rol");
            Integer rolId = null;
            if (rolData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rolMap = (Map<String, Object>) rolData;
                rolId = (Integer) rolMap.get("id");
            } else {
                rolId = (Integer) nuevoUsuarioMap.get("rolId");
                if (rolId == null) rolId = (Integer) nuevoUsuarioMap.get("rol");
            }
            if (rolId == null || rolId <= 0) {
                return ResponseEntity.badRequest().body("Debe seleccionar un rol válido");
            }

            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(password);
            nuevoUsuario.setEstado(estado != null ? estado : "ACTIVO");
            usuarioService.asignarRolPorId(nuevoUsuario, rolId);
            Usuario usuarioCreado = usuarioService.guardar(nuevoUsuario);

            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            HistorialCambios historial = new HistorialCambios();
            historial.setAccion("CREAR");
            historial.setDescripcion("Admin creó nuevo usuario: " + usuarioCreado.getNombre());
            historial.setEntidadAfectada("USUARIO");
            historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
            historial.setUsuario(usuarioCreado);
            historial.setFechaCambio(LocalDateTime.now());
            historialService.save(historial);

            return ResponseEntity.ok("Usuario creado correctamente");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/api/usuarios/{id}/eliminar-permanente")
    @ResponseBody
    public Map<String, Object> eliminarPermanente(
            @PathVariable Integer id,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Verificar que sea admin
            Usuario admin = (Usuario) session.getAttribute("usuario");
            if (admin == null || !admin.isAdmin()) {
                response.put("success", false);
                response.put("message", "No autorizado");
                return response;
            }

            // No permitir eliminarse a sí mismo
            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                response.put("success", false);
                response.put("message", "No puedes eliminarte a ti mismo");
                return response;
            }

            // Eliminar permanentemente
            boolean exito = usuarioService.eliminarPermanente(id);

            if (exito) {
                // Registrar en auditoría
                String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR_PERMANENTE");
                historial.setDescripcion("Admin eliminó permanentemente al usuario ID: " + id);
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setFechaCambio(LocalDateTime.now());
                historialService.save(historial);

                response.put("success", true);
                response.put("message", "Usuario eliminado permanentemente");
            } else {
                response.put("success", false);
                response.put("message", "No se encontró el usuario");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}