package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import com.easydates.easydateap.service.ITareaService;
import com.easydates.easydateap.service.IUsuarioService;  // ← NUEVO IMPORT
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ITareaService tareaService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IEtiquetaService etiquetaService;

    @Autowired
    private IUsuarioService usuarioService;  // ← NUEVO: Para actualizar estado premium

    @GetMapping("/home")
    public String inicioCliente(HttpSession session, Model model) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuarioActualizado = usuarioService.findById(usuarioId).orElse(null);

        // Verificar si hay sesión activa
        String nombre = (String) session.getAttribute("usuarioLogueado");
        String rol = (String) session.getAttribute("rolUsuario");

        if (usuarioId == null || nombre == null) {
            System.out.println("No hay sesión - Redirigiendo al login");
            return "redirect:/login";
        }

        if (usuarioActualizado != null) {
            // Actualizar sesión con usuario actualizado
            session.setAttribute("usuario", usuarioActualizado);
            model.addAttribute("usuario", usuarioActualizado);
            model.addAttribute("esPremium", usuarioActualizado.getEsPremium());
            model.addAttribute("fechaPremiumExpiracion", usuarioActualizado.getFechaPremiumExpiracion());
        } else {
            // Fallback si no se encuentra el usuario
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario != null) {
                model.addAttribute("usuario", usuario);
                model.addAttribute("esPremium", usuario.getEsPremium());
            } else {
                Map<String, String> usuarioMap = new HashMap<>();
                usuarioMap.put("nombre", nombre);
                usuarioMap.put("rol", rol != null ? rol : "USUARIO");
                model.addAttribute("usuario", usuarioMap);
                model.addAttribute("esPremium", false);
            }
        }

        // Estadísticas desde BD
        model.addAttribute("totalTareas", tareaService.contarTotal(usuarioId));
        model.addAttribute("tareasPendientesCount", tareaService.contarPendientes(usuarioId));
        model.addAttribute("tareasCompletadas", tareaService.contarCompletadas(usuarioId));
        model.addAttribute("tareasUrgentes", tareaService.contarUrgentes(usuarioId));

        // Tareas por estado desde BD
        model.addAttribute("tareasPendientes", tareaService.listarPorEstado(usuarioId, "PENDIENTE"));
        model.addAttribute("tareasEnProgreso", tareaService.listarPorEstado(usuarioId, "EN_PROGRESO"));
        model.addAttribute("tareasTerminadas", tareaService.listarPorEstado(usuarioId, "TERMINADO"));
        model.addAttribute("todasLasTareas", tareaService.listarTareasPorUsuario(usuarioId));

        // Categorías y etiquetas desde BD
        model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));
        model.addAttribute("etiquetas", etiquetaService.listarTodas());

        return "client/home";
    }

    // ✅ MÉTODO NUEVO: Verificar y actualizar estado premium
    private Usuario verificarYActualizarEstadoPremium(Integer usuarioId) {
        try {
            // Obtener usuario actualizado desde BD
            Usuario usuario = usuarioService.findById(usuarioId).orElse(null);

            if (usuario == null) {
                return null;
            }

            // Si el usuario es premium, verificar si la suscripción está vencida
            if (usuario.getEsPremium() && usuario.getFechaPremiumExpiracion() != null) {
                LocalDate hoy = LocalDate.now();

                // Si la fecha de expiración ya pasó, desactivar premium
                if (usuario.getFechaPremiumExpiracion().isBefore(hoy) ||
                        usuario.getFechaPremiumExpiracion().isEqual(hoy)) {

                    System.out.println("⚠️ Suscripción vencida para usuario: " + usuario.getNombre());

                    // Actualizar usuario a no premium
                    usuario.setEsPremium(false);
                    usuario.setFechaPremiumExpiracion(null);
                    usuarioService.actualizar(usuarioId, usuario);

                    System.out.println("✅ Estado premium actualizado a FALSE");
                }
            }

            return usuario;

        } catch (Exception e) {
            System.err.println("❌ Error al verificar estado premium: " + e.getMessage());
            return null;
        }
    }

    @GetMapping("/tareas")
    public String tareas(
            HttpSession session,
            Model model,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "fechaLimite") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        System.out.println(" [DEBUG] Cargando página de tareas...");

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            System.out.println(" No hay sesión");
            return "redirect:/login";
        }

        System.out.println("🔍 [DEBUG] Usuario ID: " + usuarioId);
        System.out.println("🔍 [DEBUG] Filtros: estado=" + estado + ", prioridad=" + prioridad + ", categoria=" + categoria + ", q=" + q);

        // Obtener tareas con filtros
        List<Tarea> tareas = tareaService.listarConFiltros(usuarioId, estado, prioridad, categoria, q, sortBy, sortDir);

        System.out.println("[DEBUG] Tareas encontradas: " + tareas.size());
        for (Tarea t : tareas) {
            System.out.println("   - " + t.getTitulo() + " (" + t.getEstadoTarea() + ")");
        }

        // Agregar al modelo
        model.addAttribute("tareas", tareas);
        model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));

        // Mantener filtros en vista
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroPrioridad", prioridad);
        model.addAttribute("filtroCategoria", categoria);
        model.addAttribute("busqueda", q);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "client/tareas";
    }
    @GetMapping("/api/usuario/verificar-premium")
    @ResponseBody
    public Map<String, Object> verificarEstadoPremium(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("esPremium", false);
            response.put("suscripcionActiva", false);
            return response;
        }

        // Obtener usuario actualizado desde BD
        Usuario usuario = usuarioService.findById(usuarioId).orElse(null);
        if (usuario == null) {
            response.put("esPremium", false);
            response.put("suscripcionActiva", false);
            return response;
        }

        // Verificar si tiene suscripción activa
        boolean suscripcionActiva = false;
        if (usuario.getEsPremium() && usuario.getFechaPremiumExpiracion() != null) {
            suscripcionActiva = usuario.getFechaPremiumExpiracion().isAfter(LocalDate.now());

            // Si está vencida, actualizar
            if (!suscripcionActiva) {
                usuario.setEsPremium(false);
                usuario.setFechaPremiumExpiracion(null);
                usuarioService.actualizar(usuarioId, usuario);
            }
        }

        response.put("esPremium", usuario.getEsPremium());
        response.put("suscripcionActiva", suscripcionActiva);
        response.put("rol", usuario.getRol() != null ? usuario.getRol().getNombre() : "Usuario");

        return response;
    }
}