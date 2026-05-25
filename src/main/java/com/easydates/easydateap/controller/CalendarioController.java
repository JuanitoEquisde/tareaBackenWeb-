package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Tarea;
import com.easydates.easydateap.service.ITareaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class CalendarioController {

    @Autowired
    private ITareaService tareaService;

    @GetMapping("/calendario")
    public String mostrarCalendario(
            HttpSession session,
            Model model,
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer anio) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        // Fecha actual o solicitada
        LocalDate fechaActual = LocalDate.now();
        int mesActual = (mes != null) ? mes : fechaActual.getMonthValue();
        int anioActual = (anio != null) ? anio : fechaActual.getYear();

        // Obtener tareas del usuario
        List<Tarea> todasLasTareas = tareaService.listarTareasPorUsuario(usuarioId);

        // Filtrar tareas del mes seleccionado
        YearMonth yearMonth = YearMonth.of(anioActual, mesActual);
        LocalDate primerDiaMes = yearMonth.atDay(1);
        LocalDate ultimoDiaMes = yearMonth.atEndOfMonth();

        List<Tarea> tareasDelMes = todasLasTareas.stream()
                .filter(t -> t.getFechaLimite() != null)
                .filter(t -> !t.getFechaLimite().isBefore(primerDiaMes) && !t.getFechaLimite().isAfter(ultimoDiaMes))
                .collect(Collectors.toList());

        // Agrupar tareas por día
        Map<LocalDate, List<Tarea>> tareasPorDia = tareasDelMes.stream()
                .filter(t -> t.getFechaLimite() != null)
                .collect(Collectors.groupingBy(Tarea::getFechaLimite));

        // Calcular días del mes
        int diasEnMes = yearMonth.lengthOfMonth();
        int primerDiaSemana = primerDiaMes.getDayOfWeek().getValue();

        // Calcular días del mes anterior
        YearMonth mesAnteriorYM = (mesActual > 1)
                ? YearMonth.of(anioActual, mesActual - 1)
                : YearMonth.of(anioActual - 1, 12);
        int diasMesAnterior = mesAnteriorYM.lengthOfMonth();

        // Fecha actual real
        LocalDate hoy = LocalDate.now();

        // Datos del calendario (usuario, categorias, etiquetas ya se agregan automáticamente)
        model.addAttribute("mesActual", mesActual);
        model.addAttribute("anioActual", anioActual);
        model.addAttribute("nombreMes", obtenerNombreMes(mesActual));
        model.addAttribute("diasEnMes", diasEnMes);
        model.addAttribute("primerDiaSemana", primerDiaSemana);
        model.addAttribute("tareasPorDia", tareasPorDia);
        model.addAttribute("mesAnterior", mesActual == 1 ? 12 : mesActual - 1);
        model.addAttribute("anioAnterior", mesActual == 1 ? anioActual - 1 : anioActual);
        model.addAttribute("mesSiguiente", mesActual == 12 ? 1 : mesActual + 1);
        model.addAttribute("anioSiguiente", mesActual == 12 ? anioActual + 1 : anioActual);
        model.addAttribute("diasMesAnterior", diasMesAnterior);
        model.addAttribute("diaActual", hoy.getDayOfMonth());
        model.addAttribute("mesActualHoy", hoy.getMonthValue());
        model.addAttribute("anioActualHoy", hoy.getYear());

        return "client/calendario";
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {
                "", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return meses[mes];
    }
}