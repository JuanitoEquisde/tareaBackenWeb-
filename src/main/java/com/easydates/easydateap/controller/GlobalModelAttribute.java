package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttribute {

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IEtiquetaService etiquetaService;

    @ModelAttribute
    public void addGlobalAttributes(HttpSession session, org.springframework.ui.Model model) {
        // Obtener usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");

        if (usuario != null && usuarioId != null) {
            model.addAttribute("usuario", usuario);

            if (usuario.getRol() != null) {
                model.addAttribute("nombreRol", usuario.getRol().getNombre().toUpperCase());
            } else {
                model.addAttribute("nombreRol", "Usuario");
            }

            // Agregar categorías y etiquetas automáticamente
            model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));
            model.addAttribute("etiquetas", etiquetaService.listarTodas());
        }
    }
}