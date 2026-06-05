package com.easydates.easydateap.controller;

import com.easydates.easydateap.dto.CustomerSuccessDTO;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ICustomerSuccessService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Controller
@RequestMapping("/admin/customer-success")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminCustomerSuccessController {

    @Autowired
    private ICustomerSuccessService customerSuccessService;

    @GetMapping
    public String customerSuccessDashboard(
            @RequestParam(required = false) String filtro,
            Model model,
            HttpSession session) {

        // Validar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        // Obtener métricas generales
        Map<String, Object> metricas = customerSuccessService.obtenerMetricasGenerales();
        model.addAttribute("metricas", metricas);

        // Obtener lista de usuarios según filtro
        List<CustomerSuccessDTO> usuarios;
        if (filtro == null || filtro.isEmpty() || "todos".equals(filtro)) {
            usuarios = customerSuccessService.obtenerTodosLosUsuariosConHealthScore();
        } else if ("riesgo".equals(filtro)) {
            usuarios = customerSuccessService.obtenerUsuariosEnRiesgo("ALTO")
                    .stream().limit(20).toList();
        } else if ("upsell".equals(filtro)) {
            usuarios = customerSuccessService.obtenerOportunidadesUpsell()
                    .stream().limit(20).toList();
        } else {
            usuarios = customerSuccessService.obtenerTodosLosUsuariosConHealthScore();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("filtroActual", filtro != null ? filtro : "todos");
        model.addAttribute("activePage", "customer-success");

        return "admin/customer-success";
    }

    @GetMapping("/{id}/detalle")
    @ResponseBody
    public Map<String, Object> obtenerDetalleUsuario(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        CustomerSuccessDTO usuario = customerSuccessService.obtenerDetalleUsuario(id);

        if (usuario != null) {
            response.put("success", true);
            response.put("usuario", usuario);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
        }

        return response;
    }
}