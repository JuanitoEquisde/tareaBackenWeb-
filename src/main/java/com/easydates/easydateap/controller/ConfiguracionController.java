package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.CambioPasswordDTO;
import com.easydates.easydateap.dto.PerfilDTO;
import com.easydates.easydateap.model.Etiqueta;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.service.ICategoriaService;
import com.easydates.easydateap.service.IEtiquetaService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/cliente/configuracion")
public class ConfiguracionController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private ICategoriaService categoriaService;

    @Autowired
    private IEtiquetaService etiquetaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Mostrar página de configuración
    @GetMapping
    public String mostrarConfiguracion(HttpSession session, Model model) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Cargar datos del usuario
        Optional<Usuario> usuarioOpt = usuarioService.findByEmail((String) session.getAttribute("email"));
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            model.addAttribute("usuarioActual", usuario);

        }

        // Cargar categorías y etiquetas del usuario
        model.addAttribute("categorias", categoriaService.listarPorUsuario(usuarioId));
        model.addAttribute("etiquetas", etiquetaService.listarTodas());

        return "client/configuracion";
    }

    // Actualizar perfil
    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute PerfilDTO perfilDTO, HttpSession session, RedirectAttributes redirectAttributes) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioService.findByEmail((String) session.getAttribute("email"));
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                usuario.setNombre(perfilDTO.getNombre());
                usuario.setEmail(perfilDTO.getEmail());

                usuarioService.guardar(usuario);

                // Actualizar sesión
                session.setAttribute("usuarioLogueado", usuario.getNombre());

                redirectAttributes.addFlashAttribute("mensaje", "✅ Perfil actualizado correctamente");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Error al actualizar: " + e.getMessage());
        }

        return "redirect:/cliente/configuracion";
    }

    // Cambiar contraseña
    @PostMapping("/cambiar-password")
    @ResponseBody
    public ResponseEntity<?> cambiarPassword(@RequestBody CambioPasswordDTO cambioDTO, HttpSession session) {
        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("❌ Sesión expirada");
        }

        try {
            Optional<Usuario> usuarioOpt = usuarioService.findByEmail((String) session.getAttribute("email"));
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();

                // Verificar password actual
                if (!passwordEncoder.matches(cambioDTO.getPasswordActual(), usuario.getPassword())) {
                    return ResponseEntity.badRequest().body("❌ La contraseña actual es incorrecta");
                }

                // Verificar que las nuevas contraseñas coincidan
                if (!cambioDTO.getPasswordNuevo().equals(cambioDTO.getPasswordConfirmacion())) {
                    return ResponseEntity.badRequest().body("❌ Las nuevas contraseñas no coinciden");
                }

                // Actualizar contraseña
                usuario.setPassword(passwordEncoder.encode(cambioDTO.getPasswordNuevo()));
                usuarioService.guardar(usuario);

                return ResponseEntity.ok("✅ Contraseña cambiada correctamente");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error: " + e.getMessage());
        }

        return ResponseEntity.status(500).body("❌ Error desconocido");
    }


    // CRUD Etiquetas
    @PostMapping("/etiqueta/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarEtiqueta(@RequestParam String nombre, @RequestParam(required = false) String color, HttpSession session) {
        try {
            Etiqueta etiqueta = etiquetaService.crearEtiqueta(nombre, color);
            return ResponseEntity.ok(etiqueta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    @PostMapping("/etiqueta/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<?> eliminarEtiqueta(@PathVariable Integer id, HttpSession session) {
        try {
            if (etiquetaService.eliminarEtiqueta(id)) {
                return ResponseEntity.ok("✅ Etiqueta eliminada");
            }
            return ResponseEntity.badRequest().body("❌ No se pudo eliminar");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }
}