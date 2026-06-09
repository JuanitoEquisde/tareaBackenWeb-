package com.easydates.easydateap.controller;

import com.easydates.easydateap.model.Rol;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.repository.RolRepository; // ✅ IMPORTANTE
import com.easydates.easydateap.service.IUsuarioService;
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
    private PasswordEncoder passwordEncoder;

    // AGREGAR ESTO: Inyectar RolRepository
    @Autowired
    private RolRepository rolRepository;

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

        // OBTENER EL ROL DESDE LA BASE DE DATOS (NO CREAR NUEVO OBJETO)
        Rol rolEstandar = rolRepository.findById(2)  // ID 2 = Usuario Estándar
                .orElseThrow(() -> new RuntimeException("Rol 'Usuario Estándar' no encontrado"));

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre.trim());
        nuevoUsuario.setEmail(email.trim().toLowerCase());
        nuevoUsuario.setPassword(password); // Se encriptará en el service
        nuevoUsuario.setEstado("ACTIVO");

        // ASIGNAR EL ROL PERSISTIDO (MANAGED ENTITY)
        nuevoUsuario.setRol(rolEstandar);

        // Guardar en la base de datos
        usuarioService.guardar(nuevoUsuario);

        redirectAttributes.addFlashAttribute("mensaje", "✅ Registro exitoso. ¡Bienvenido a NotyGo!");
        return "redirect:/login";
    }
}