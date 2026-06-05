package com.easydates.easydateap.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cliente")
public class PerfilController {

    // Verificar sesión (helper)
    private boolean haySesionActiva(HttpSession session) {
        return session.getAttribute("usuarioId") != null;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(HttpSession session, Model model) {
        // Redirigir al login si no hay sesión
        if (!haySesionActiva(session)) {
            return "redirect:/login";
        }

        // Obtener datos de sesión
        String nombre = (String) session.getAttribute("usuarioLogueado");
        String email = (String) session.getAttribute("email");
        String rol = (String) session.getAttribute("rolUsuario");

        // Pasar datos a la vista
        model.addAttribute("usuarioNombre", nombre);
        model.addAttribute("usuarioEmail", email);
        model.addAttribute("usuarioRol", rol);

        return "client/perfil";
    }
}
