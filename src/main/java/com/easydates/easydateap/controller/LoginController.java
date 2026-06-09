package com.easydates.easydateap.controller;

import jakarta.servlet.http.HttpSession;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private IUsuarioService usuarioService;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/login/invitado")
    public String loginInvitado(HttpSession session) {
        session.setAttribute("usuarioLogueado", "Invitado Anónimo");
        session.setAttribute("rolUsuario", "INVITADO");
        session.setAttribute("esInvitado", true);
        session.setAttribute("usuarioId", 0);
        return "redirect:/cliente/home";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("email") String email,
                                @RequestParam("password") String password,
                                HttpSession session,
                                Model model) {

        System.out.println("🔐 Intentando login para: " + email);

        Optional<Usuario> usuarioOpt = usuarioService.login(email, password);
        if (usuarioOpt.isEmpty()) {
            System.out.println("❌ [CONTROLLER] usuarioService.login() retornó Optional.empty()");
            System.out.println("   Posibles causas: contraseña incorrecta, usuario inactivo, o filtro de rol");
        }


        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            // ✅ IMPORTANTE: Verificar que el rol no sea null
            if (usuario.getRol() == null) {
                System.err.println("ERROR: Usuario " + email + " no tiene rol asignado!");
                model.addAttribute("error", "Usuario sin rol asignado. Contacta al admin.");
                return "login";
            }

            String rolNombre = usuario.getRol().getNombre();
            System.out.println("✅ Acceso permitido a: " + usuario.getNombre() + " (ID: " + usuario.getId() + ")");
            System.out.println("🔑 Rol del usuario: " + rolNombre);

            // ✅ 1. Crear sesión manual (para tu uso)
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioLogueado", usuario.getNombre());
            session.setAttribute("email", usuario.getEmail());
            session.setAttribute("rolUsuario", rolNombre.toUpperCase());
            session.setAttribute("esInvitado", false);
            session.setAttribute("usuario", usuario); // ✅ Guardar el objeto completo

            // ✅ 2. AUTENTICAR EN SPRING SECURITY
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    email,  // principal
                    password,  // credentials
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase()))  // authorities
            );

            // Guardar en SecurityContext
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Guardar en sesión HTTP
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            // ✅ 3. REDIRECCIÓN SEGÚN ROL (VERIFICACIÓN EXPLÍCITA)
            String rolUpper = rolNombre.toUpperCase().trim();
            System.out.println("🎯 Rol normalizado: '" + rolUpper + "'");

            if ("ADMINISTRADOR".equals(rolUpper)) {
                System.out.println("✅ Redirigiendo a ADMIN dashboard...");
                return "redirect:/admin/dashboard";
            } else {
                System.out.println("✅ Redirigiendo a CLIENTE home...");
                return "redirect:/cliente/home";
            }
        }

        System.out.println("❌ Credenciales incorrectas para: " + email);
        model.addAttribute("error", "Correo o contraseña incorrecta");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }
}