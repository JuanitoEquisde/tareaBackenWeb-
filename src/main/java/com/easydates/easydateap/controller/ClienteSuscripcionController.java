package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.SuscripcionDTO;
import com.easydates.easydateap.entity.*;
import com.easydates.easydateap.repository.RolRepository;
import com.easydates.easydateap.service.ISuscripcionService;
import com.easydates.easydateap.service.IUsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/suscripciones")
public class ClienteSuscripcionController {

    @Autowired
    private ISuscripcionService suscripcionService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepository;

    @GetMapping("/comprar")
    public String mostrarPlanes(@RequestParam(required = false) Integer planId,
                                Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Plan> planes = suscripcionService.listarPlanesActivos();
        Optional<Suscripcion> suscripcionActiva = suscripcionService.getSuscripcionActiva(usuario.getId());

        model.addAttribute("planes", planes);
        model.addAttribute("suscripcionActiva", suscripcionActiva.orElse(null));
        model.addAttribute("usuario", usuario);
        model.addAttribute("planId", planId);

        return "client/comprar-premium";
    }

    @PostMapping("/procesar-pago")
    @ResponseBody
    public ResponseEntity<?> procesarPago(@RequestBody SuscripcionDTO dto, HttpSession session) {
        // ✅ Obtener usuario de sesión (más seguro)
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null || usuario.getId() == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Debes iniciar sesión para comprar");
            return ResponseEntity.status(401).body(error);
        }

        try {
            // Validación básica de tarjeta
            if (dto.getNumeroTarjeta() == null || dto.getNumeroTarjeta().replaceAll("\\s", "").length() < 16) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Número de tarjeta inválido");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getCvv() == null || dto.getCvv().length() < 3) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "CVV inválido");
                return ResponseEntity.badRequest().body(error);
            }

            if (dto.getPlanId() == null || dto.getPlanId() <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Plan no válido");
                return ResponseEntity.badRequest().body(error);
            }

            // ✅ Crear suscripción pasando el ID del usuario de sesión
            Suscripcion suscripcion = suscripcionService.crearSuscripcion(dto, usuario.getId());

            // ✅ CAMBIAR ROL A PREMIUM
            Optional<Rol> rolPremium = rolRepository.findByNombre("PREMIUM");
            if (rolPremium.isPresent()) {
                usuario.setRol(rolPremium.get());
                usuario.setEsPremium(true);
                usuarioService.actualizar(usuario.getId(), usuario);

                // ✅ Actualizar sesión con el nuevo rol
                session.setAttribute("usuario", usuario);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "¡Suscripción activada exitosamente!");
            response.put("numeroTransaccion", suscripcion.getNumeroTransaccion());
            response.put("fechaFin", suscripcion.getFechaFin().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error al procesar el pago: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/mi-plan")
    public String miPlan(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        Optional<Suscripcion> suscripcion = suscripcionService.getSuscripcionActiva(usuario.getId());

        model.addAttribute("suscripcion", suscripcion.orElse(null));
        model.addAttribute("usuario", usuario);

        return "client/mi-plan";
    }
}