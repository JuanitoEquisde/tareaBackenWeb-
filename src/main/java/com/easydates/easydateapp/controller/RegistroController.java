package com.easydates.easydateapp.controller;

import com.easydates.easydateapp.entity.Rol;
import com.easydates.easydateapp.entity.Usuario;
import com.easydates.easydateapp.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistroController {

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ Para encriptar contraseñas

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@RequestParam String nombre,
                                   @RequestParam String email,
                                   @RequestParam String password,
                                   RedirectAttributes redirectAttributes) {

        // Validar que el email no exista
        if (usuarioService.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "❌ Este email ya está registrado");
            return "redirect:/registro";
        }

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setEmail(email);

        // ✅ ENCRYPTAR CONTRASEÑA CON BCrypt (IMPORTANTE)
        nuevoUsuario.setPassword(passwordEncoder.encode(password));

        nuevoUsuario.setEstado("ACTIVO");

        // Rol por defecto: Usuario Estándar (id=2)
        Rol rolEstandar = new Rol();
        rolEstandar.setId(2);
        nuevoUsuario.setRol(rolEstandar);

        // Guardar en la base de datos
        usuarioService.guardar(nuevoUsuario);

        redirectAttributes.addFlashAttribute("mensaje", "✅ Registro exitoso. ¡Bienvenido a NotyGo!");
        return "redirect:/login";
    }
}