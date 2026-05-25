package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import com.easydates.easydateap.service.ITareaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/home")
    public String inicioCliente(HttpSession session, Model model) {

        // Verificar si hay sesión activa
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        String nombre = (String) session.getAttribute("usuarioLogueado");
        String rol = (String) session.getAttribute("rolUsuario");

        if (usuarioId == null || nombre == null) {
            System.out.println("No hay sesión - Redirigiendo al login");
            return "redirect:/login";
        }


        System.out.println("✅ Acceso permitido a: " + nombre + " (ID: " + usuarioId + ")");

        // Datos del usuario
        Map<String, String> usuarioMap = new HashMap<>();
        usuarioMap.put("nombre", nombre);
        usuarioMap.put("rol", rol != null ? rol : "USUARIO");
        model.addAttribute("usuario", usuarioMap);

        //Estadísticas desde BD
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
}