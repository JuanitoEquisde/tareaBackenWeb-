package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.HistorialCambios;
import com.easydates.easydateap.service.IHistorialCambiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/auditoria")
public class AdminAuditoriaController {

    @Autowired
    private IHistorialCambiosService historialService;

    @GetMapping
    public String mostrarAuditoria(
            @RequestParam(required = false) String filtroAccion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaCambio") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Normalizar filtro
        String accionBusqueda = (filtroAccion != null && !filtroAccion.trim().isEmpty())
                ? filtroAccion.trim().toUpperCase()
                : null;

        // Crear Pageable con ordenamiento
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Crear Pageable
        Pageable pageable = PageRequest.of(page, size, sort);

        // Obtener página del servicio
        Page<HistorialCambios> paginaHistorial = historialService.buscarHistorialPaginado(
                accionBusqueda, pageable
        );

        // Pasar datos a la vista
        model.addAttribute("historiales", paginaHistorial.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaHistorial.getTotalPages());
        model.addAttribute("totalElements", paginaHistorial.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("totalCambios", paginaHistorial.getTotalElements());
        model.addAttribute("filtroAccion", filtroAccion);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("activePage", "auditoria");

        return "admin/auditoria";
    }
}