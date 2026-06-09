package com.easydates.easydateap.controller;

import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import com.easydates.easydateap.service.ITareaService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.HashMap;
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
    private IUsuarioService usuarioService;

    @GetMapping("/home")
    public String inicioCliente(HttpSession session, Model model) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return "redirect:/login";
        }

        Usuario usuarioActualizado = usuarioService.findById(usuarioId).orElse(null);

        String nombre = (String) session.getAttribute("usuarioLogueado");
        String rol = (String) session.getAttribute("rolUsuario");

        if (usuarioId == null || nombre == null) {
            System.out.println("No hay sesión - Redirigiendo al login");
            return "redirect:/login";
        }

        if (usuarioActualizado != null) {
            session.setAttribute("usuario", usuarioActualizado);
            model.addAttribute("usuario", usuarioActualizado);
            model.addAttribute("esPremium", usuarioActualizado.getEsPremium());
            model.addAttribute("fechaPremiumExpiracion", usuarioActualizado.getFechaPremiumExpiracion());
        } else {
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

        model.addAttribute("totalTareas", tareaService.contarTotal(usuarioId));
        model.addAttribute("tareasPendientesCount", tareaService.contarPendientes(usuarioId));
        model.addAttribute("tareasCompletadas", tareaService.contarCompletadas(usuarioId));
        model.addAttribute("tareasUrgentes", tareaService.contarUrgentes(usuarioId));

        model.addAttribute("tareasPendientes", tareaService.listarPorEstado(usuarioId, "PENDIENTE"));
        model.addAttribute("tareasEnProgreso", tareaService.listarPorEstado(usuarioId, "EN_PROGRESO"));
        model.addAttribute("tareasTerminadas", tareaService.listarPorEstado(usuarioId, "TERMINADO"));
        model.addAttribute("todasLasTareas", tareaService.listarTareasPorUsuario(usuarioId));

        model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));
        model.addAttribute("etiquetas", etiquetaService.listarTodas());

        return "client/home";
    }

    private Usuario verificarYActualizarEstadoPremium(Integer usuarioId) {
        try {
            Usuario usuario = usuarioService.findById(usuarioId).orElse(null);

            if (usuario == null) {
                return null;
            }

            if (usuario.getEsPremium() && usuario.getFechaPremiumExpiracion() != null) {
                LocalDate hoy = LocalDate.now();

                if (usuario.getFechaPremiumExpiracion().isBefore(hoy) ||
                        usuario.getFechaPremiumExpiracion().isEqual(hoy)) {

                    System.out.println(" Suscripción vencida para usuario: " + usuario.getNombre());

                    usuario.setEsPremium(false);
                    usuario.setFechaPremiumExpiracion(null);
                    usuarioService.actualizar(usuarioId, usuario);

                    System.out.println(" Estado premium actualizado a FALSE");
                }
            }

            return usuario;

        } catch (Exception e) {
            System.err.println("rror al verificar estado premium: " + e.getMessage());
            return null;
        }
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

        Usuario usuario = usuarioService.findById(usuarioId).orElse(null);
        if (usuario == null) {
            response.put("esPremium", false);
            response.put("suscripcionActiva", false);
            return response;
        }

        boolean suscripcionActiva = false;
        if (usuario.getEsPremium() && usuario.getFechaPremiumExpiracion() != null) {
            suscripcionActiva = usuario.getFechaPremiumExpiracion().isAfter(LocalDate.now());

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