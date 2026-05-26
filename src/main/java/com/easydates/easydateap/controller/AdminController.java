package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.HistorialCambios;
import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.IHistorialCambiosService;
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
    // ✅ API: Actualizar usuario - CORREGIDO PARA ROLES
    // =====================================================
    @PutMapping("/api/usuarios/{id}/actualizar")
    @ResponseBody
    public ResponseEntity<?> actualizarUsuario(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> usuarioActualizadoMap) {

        System.out.println("🔧 API: Actualizando usuario ID: " + id);
        System.out.println("📥 Datos recibidos: " + usuarioActualizadoMap);

        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            // Actualizar campos simples
            if (usuarioActualizadoMap.get("nombre") != null) {
                String nombre = (String) usuarioActualizadoMap.get("nombre");
                if (!nombre.trim().isEmpty()) {
                    usuario.setNombre(nombre.trim());
                }
            }

            if (usuarioActualizadoMap.get("email") != null) {
                String email = (String) usuarioActualizadoMap.get("email");
                if (!email.trim().isEmpty()) {
                    usuario.setEmail(email.trim());
                }
            }

            if (usuarioActualizadoMap.get("estado") != null) {
                String estado = (String) usuarioActualizadoMap.get("estado");
                if (!estado.trim().isEmpty()) {
                    usuario.setEstado(estado.trim());
                }
            }

            // Contraseña (solo si se proporciona y no está vacía)
            if (usuarioActualizadoMap.get("password") != null) {
                String newPassword = (String) usuarioActualizadoMap.get("password");
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    // El service se encarga de encriptar
                    usuario.setPassword(newPassword);
                }
            }

            // ✅ Manejo FLEXIBLE de rol: acepta "rolId": 2 o "rol": {"id": 2}
            Object rolData = usuarioActualizadoMap.get("rol");
            Integer rolId = null;

            if (rolData instanceof Map) {
                // Formato: { "rol": { "id": 2 } }
                @SuppressWarnings("unchecked")
                Map<String, Object> rolMap = (Map<String, Object>) rolData;
                rolId = (Integer) rolMap.get("id");
                System.out.println("🎯 Rol desde objeto: ID=" + rolId);
            } else {
                // Formato: { "rolId": 2 } o { "rol": 2 }
                rolId = (Integer) usuarioActualizadoMap.get("rolId");
                if (rolId == null) {
                    rolId = (Integer) usuarioActualizadoMap.get("rol");
                }
                System.out.println("🎯 Rol desde número: ID=" + rolId);
            }

            // Asignar rol si es válido
            if (rolId != null && rolId > 0) {
                usuarioService.asignarRol(id, rolId);
                System.out.println("✅ Rol asignado: " + rolId);
            }

            // Guardar cambios
            usuarioService.actualizar(id, usuario);
            System.out.println("✅ Usuario actualizado correctamente");

            return ResponseEntity.ok("Usuario actualizado correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al actualizar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al actualizar: " + e.getMessage());
        }
    }

    // =====================================================
    // API: Cambiar estado de usuario
    // =====================================================
    @PostMapping("/api/usuarios/{id}/cambiar-estado")
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
    // VISTA: Dashboard
    // =====================================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        System.out.println("📊 Accediendo a vista: /admin/dashboard");

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        Map<String, Object> stats = usuarioService.obtenerEstadisticas();
        model.addAttribute("stats", stats);

        List<Usuario> recientes = usuarioService.buscarConFiltrosOr(null, null, null, null)
                .stream()
                .limit(5)
                .toList();
        model.addAttribute("usuariosRecientes", recientes);

        return "admin/dashboard";
    }

    // =====================================================
    // ✅ VISTA: Listar usuarios con PAGINACIÓN - CORREGIDO
    // =====================================================
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model,
            HttpSession session) {

        nombre = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;
        email = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        estado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        Integer idBusqueda = (id != null && id > 0) ? id : null;

        List<Usuario> todosUsuarios = usuarioService.buscarConFiltros(idBusqueda,nombre, email, estado, null);


        // Paginación
        int paginaActual = (page != null && page > 0) ? page : 1;
        int tamanoPagina = (size != null && size > 0) ? size : 10;
        int totalElementos = todosUsuarios.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);
        int startIndex = (paginaActual - 1) * tamanoPagina;
        int endIndex = Math.min(startIndex + tamanoPagina, totalElementos);
        List<Usuario> usuariosPaginados = todosUsuarios.subList(startIndex, endIndex);

        // ✅ Pasar datos a la vista
        model.addAttribute("filtroId", idBusqueda);
        model.addAttribute("usuarios", usuariosPaginados);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("tamanoPagina", tamanoPagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalElementos", totalElementos);
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroEmail", email);
        model.addAttribute("filtroEstado", estado);

        return "admin/usuarios";
    }

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

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/cliente/home";
        }

        int paginaActual = (page != null && page > 0) ? page : 1;
        int tamanoPagina = (size != null && size > 0) ? size : 10;

        List<Tarea> todasLasTareas = tareaService.buscarTareasAdmin(titulo, prioridad, estado, usuario);

        int totalElementos = todasLasTareas.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);

        int startIndex = (paginaActual - 1) * tamanoPagina;
        int endIndex = Math.min(startIndex + tamanoPagina, totalElementos);

        List<Tarea> tareasPaginadas = (startIndex < totalElementos)
                ? todasLasTareas.subList(startIndex, endIndex)
                : Collections.emptyList();

        long total = todasLasTareas.size();
        long pendientes = todasLasTareas.stream().filter(t -> "PENDIENTE".equals(t.getEstadoTarea())).count();
        long enProgreso = todasLasTareas.stream().filter(t -> "EN_PROGRESO".equals(t.getEstadoTarea())).count();
        long terminadas = todasLasTareas.stream().filter(t -> "TERMINADO".equals(t.getEstadoTarea())).count();

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

    // =====================================================
    // ✅ API: Eliminar usuario - LÓGICA TRADICIONAL (INACTIVO)
    // =====================================================
    @DeleteMapping("/api/usuarios/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(
            @PathVariable Integer id,
            HttpSession session) {

        System.out.println("🗑️ API: Eliminación lógica (INACTIVO) ID: " + id);

        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }

            String datosUsuario = String.format(
                    "ID: %d, Nombre: %s, Email: %s, Rol: %s",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A"
            );

            boolean exito = usuarioService.eliminarLogico(id);

            if (exito) {
                String nombreAdmin = (String) session.getAttribute("usuarioLogueado");

                HistorialCambios historial = new HistorialCambios();
                historial.setAccion("ELIMINAR");
                historial.setDescripcion("Admin cambió estado a INACTIVO. Datos: " + datosUsuario);
                historial.setEntidadAfectada("USUARIO");
                historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                historial.setUsuario(usuario);
                historial.setFechaCambio(LocalDateTime.now());

                historialService.save(historial);
                System.out.println("📝 Auditoría registrada: ELIMINAR (INACTIVO)");

                return ResponseEntity.ok("Usuario marcado como inactivo");
            } else {
                return ResponseEntity.badRequest().body("Error al cambiar estado del usuario");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // =====================================================
    // ✅ API: Eliminar usuario PERMANENTEMENTE (Hard Delete)
    // ⚠️ COMENTADO POR DEFECTO
    // =====================================================
    /*
    @DeleteMapping("/api/usuarios/{id}/eliminar-permanente")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioPermanente(...) {
        // ... código comentado
    }
    */

    // =====================================================
    // ✅✅ API: Eliminar usuario DEL SISTEMA (Soft Delete)
    // =====================================================
    @DeleteMapping("/api/usuarios/{id}/eliminar-sistema")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuarioDelSistema(
            @PathVariable Integer id,
            HttpSession session) {

        System.out.println("🗑️ API: Eliminando usuario DEL SISTEMA ID: " + id);

        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

            Integer adminId = (Integer) session.getAttribute("usuarioId");
            if (adminId != null && adminId.equals(id)) {
                System.out.println("❌ Error: Admin intentando eliminarse a sí mismo");
                return ResponseEntity.badRequest().body("No puedes eliminarte a ti mismo");
            }

            String datosUsuario = String.format(
                    "ID: %d, Nombre: %s, Email: %s, Rol: %s",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getEmail(),
                    usuario.getRol() != null ? usuario.getRol().getNombre() : "N/A"
            );

            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            System.out.println("👤 Admin que elimina: " + nombreAdmin);

            boolean exito = usuarioService.eliminarDelSistema(id, nombreAdmin);

            if (exito) {
                System.out.println("✅ Usuario eliminado del sistema exitosamente");

                try {
                    HistorialCambios historial = new HistorialCambios();
                    historial.setAccion("ELIMINAR_SISTEMA");
                    historial.setDescripcion("Admin eliminó usuario del sistema (soft delete). Datos: " + datosUsuario);
                    historial.setEntidadAfectada("USUARIO");
                    historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                    historial.setUsuario(usuario);
                    historial.setFechaCambio(LocalDateTime.now());

                    historialService.save(historial);
                    System.out.println("📝 Auditoría guardada: ELIMINAR_SISTEMA - " + usuario.getNombre());

                } catch (Exception e) {
                    System.err.println("⚠️ Advertencia: No se pudo registrar en auditoría");
                    System.err.println("   Error: " + e.getMessage());
                }

                return ResponseEntity.ok("Usuario eliminado del sistema correctamente");
            } else {
                System.err.println("❌ Error al eliminar usuario del sistema");
                return ResponseEntity.badRequest().body("Error al eliminar usuario del sistema");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al eliminar del sistema: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // =====================================================
    // ✅✅ API: Restaurar usuario eliminado del sistema
    // =====================================================
    @PutMapping("/api/usuarios/{id}/restaurar")
    @ResponseBody
    public ResponseEntity<?> restaurarUsuario(
            @PathVariable Integer id,
            HttpSession session) {

        System.out.println("♻️ API: Restaurando usuario ID: " + id);

        try {
            Usuario usuario = usuarioService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            boolean exito = usuarioService.restaurarUsuario(id);

            if (exito) {
                System.out.println("✅ Usuario restaurado exitosamente");

                try {
                    HistorialCambios historial = new HistorialCambios();
                    historial.setAccion("RESTAURAR");
                    historial.setDescripcion("Admin restauró usuario eliminado: " + usuario.getNombre() + " (" + usuario.getEmail() + ")");
                    historial.setEntidadAfectada("USUARIO");
                    historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
                    historial.setUsuario(usuario);
                    historial.setFechaCambio(LocalDateTime.now());

                    historialService.save(historial);
                    System.out.println("📝 Auditoría guardada: RESTAURAR - " + usuario.getNombre());

                } catch (Exception e) {
                    System.err.println("⚠️ Advertencia: No se pudo registrar restauración en auditoría");
                }

                return ResponseEntity.ok("Usuario restaurado correctamente");
            } else {
                return ResponseEntity.badRequest().body("El usuario no estaba eliminado o no se pudo restaurar");
            }

        } catch (Exception e) {
            System.err.println("❌ Error al restaurar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    // =====================================================
    // ✅✅ API: Crear usuario - CORREGIDO PARA ROLES
    // =====================================================
    @PostMapping("/api/usuarios/crear")
    @ResponseBody
    public ResponseEntity<?> crearUsuario(
            @RequestBody Map<String, Object> nuevoUsuarioMap,
            HttpSession session) {

        System.out.println("➕ API: Creando nuevo usuario");
        System.out.println("📥 Datos recibidos: " + nuevoUsuarioMap);

        try {
            // Extraer datos del map
            String nombre = (String) nuevoUsuarioMap.get("nombre");
            String email = (String) nuevoUsuarioMap.get("email");
            String password = (String) nuevoUsuarioMap.get("password");
            String estado = (String) nuevoUsuarioMap.get("estado");

            // Validar email
            if (usuarioService.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body("El correo electrónico ya está registrado");
            }

            // Validar contraseña
            if (password == null || password.length() < 6) {
                return ResponseEntity.badRequest().body("La contraseña debe tener al menos 6 caracteres");
            }

            // ✅ Manejo FLEXIBLE de rol
            Object rolData = nuevoUsuarioMap.get("rol");
            Integer rolId = null;

            if (rolData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> rolMap = (Map<String, Object>) rolData;
                rolId = (Integer) rolMap.get("id");
            } else {
                rolId = (Integer) nuevoUsuarioMap.get("rolId");
                if (rolId == null) {
                    rolId = (Integer) nuevoUsuarioMap.get("rol");
                }
            }

            if (rolId == null || rolId <= 0) {
                return ResponseEntity.badRequest().body("Debe seleccionar un rol válido");
            }

            // Crear usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombre(nombre);
            nuevoUsuario.setEmail(email);
            nuevoUsuario.setPassword(password);  // El service encripta
            nuevoUsuario.setEstado(estado != null ? estado : "ACTIVO");

            // Asignar rol
            usuarioService.asignarRolPorId(nuevoUsuario, rolId);

            Usuario usuarioCreado = usuarioService.guardar(nuevoUsuario);

            // Registrar en auditoría
            String nombreAdmin = (String) session.getAttribute("usuarioLogueado");
            HistorialCambios historial = new HistorialCambios();
            historial.setAccion("CREAR");
            historial.setDescripcion("Admin creó nuevo usuario: " + usuarioCreado.getNombre() + " (" + usuarioCreado.getEmail() + ")");
            historial.setEntidadAfectada("USUARIO");
            historial.setUsuarioAdmin(nombreAdmin != null ? nombreAdmin : "Admin");
            historial.setUsuario(usuarioCreado);
            historial.setFechaCambio(LocalDateTime.now());

            historialService.save(historial);
            System.out.println("📝 Auditoría guardada: CREAR - " + usuarioCreado.getNombre());

            return ResponseEntity.ok("Usuario creado correctamente");

        } catch (Exception e) {
            System.err.println("❌ Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}