package com.easydates.easydateap.controller;
//HOLAAAAAAA
import com.easydates.easydateap.entity.HistorialCambios;
import com.easydates.easydateap.service.IHistorialCambiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/auditoria")
public class AdminAuditoriaController {
//helloooooo7
    @Autowired
    private IHistorialCambiosService historialService;

    @GetMapping
    public String mostrarAuditoria(
            @RequestParam(required = false) String filtroAccion,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            Model model) {

        int paginaActual =(page != null && page > 0) ? page : 1;
        int tamanoPagina =(size != null && size > 0) ? size : 10;

        List<HistorialCambios> todosLosHistoriales;

        if (filtroAccion != null && !filtroAccion.isEmpty()) {
            todosLosHistoriales = historialService.searchByAccion(filtroAccion);
            model.addAttribute("filtroAccion", filtroAccion);
        } else {
            todosLosHistoriales = historialService.findAll();
            model.addAttribute("filtroAccion", "");
        }

        // Calcular totales para paginación
        int totalElementos = todosLosHistoriales.size();
        int totalPaginas = (int) Math.ceil((double) totalElementos / tamanoPagina);

        // Calcular índices para paginación manual
        int startIndex = (paginaActual - 1) * tamanoPagina;
        int endIndex = Math.min(startIndex + tamanoPagina, totalElementos);

        // Obtener solo la página actual
        List<HistorialCambios> historialesPaginados = (startIndex < totalElementos)
                ? todosLosHistoriales.subList(startIndex, endIndex)
                : Collections.emptyList();

        // Agregar al modelo
        model.addAttribute("historiales", historialesPaginados);
        model.addAttribute("paginaActual", paginaActual);
        model.addAttribute("tamanoPagina", tamanoPagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalElementos", totalElementos);
        model.addAttribute("totalCambios", historialService.count());

        return "admin/auditoria";
    }
}