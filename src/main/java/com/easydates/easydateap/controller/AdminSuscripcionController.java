package com.easydates.easydateap.controller;

import com.easydates.easydateap.model.Suscripcion;
import com.easydates.easydateap.model.Usuario;
import com.easydates.easydateap.repository.SuscripcionRepository;
import com.easydates.easydateap.service.ISuscripcionService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/suscripciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminSuscripcionController {

    @Autowired
    private ISuscripcionService suscripcionService;
    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @GetMapping
    public String listarSuscripciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            HttpSession session) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Suscripcion> suscripcionesPage = suscripcionService.buscarConFiltrosAdmin(
                estado, plan, usuario, fechaInicio, fechaFin, pageable);

        Map<String, Long> stats = suscripcionService.obtenerEstadisticasSuscripciones();

        model.addAttribute("suscripciones", suscripcionesPage.getContent());
        model.addAttribute("currentPage", suscripcionesPage.getNumber());
        model.addAttribute("totalPages", suscripcionesPage.getTotalPages());
        model.addAttribute("totalElements", suscripcionesPage.getTotalElements());
        model.addAttribute("pageSize", size);

        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroPlan", plan);
        model.addAttribute("filtroUsuario", usuario);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("stats", stats);
        model.addAttribute("activePage", "suscripciones");

        return "admin/suscripciones";
    }

    @PostMapping("/{id}/cambiar-estado")
    @ResponseBody
    public Map<String, Object> cambiarEstadoSuscripcion(
            @PathVariable Integer id,
            @RequestParam String nuevoEstado,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.cambiarEstadoSuscripcion(id, nuevoEstado);

            if (exito) {
                response.put("success", true);
                String mensaje = "ACTIVA".equals(nuevoEstado)
                        ? "Suscripción restaurada correctamente"
                        : "Suscripción cancelada correctamente";
                response.put("message", mensaje);
                redirectAttributes.addFlashAttribute("toastMessage", mensaje);
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo actualizar el estado de la suscripción");
            }
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Estado no válido: " + e.getMessage());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarSuscripcion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.eliminarSuscripcion(id);

            if (exito) {
                response.put("success", true);
                response.put("message", "Registro eliminado correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción eliminada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se encontró la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> obtenerDetalleSuscripcion(@PathVariable Integer id) {
        return suscripcionService.obtenerDetalleSuscripcion(id);
    }

    @PostMapping("/{id}/restaurar")
    @ResponseBody
    public Map<String, Object> restaurarSuscripcion(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.cambiarEstadoSuscripcion(id, "ACTIVA");

            if (exito) {
                response.put("success", true);
                response.put("message", "Suscripción restaurada correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción restaurada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se pudo restaurar la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @PutMapping("/{id}/editar")
    @ResponseBody
    public Map<String, Object> editarSuscripcion(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> datosActualizados,
            RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        try {
            boolean exito = suscripcionService.editarSuscripcion(id, datosActualizados);

            if (exito) {
                response.put("success", true);
                response.put("message", "Suscripción actualizada correctamente");
                redirectAttributes.addFlashAttribute("toastMessage", "Suscripción actualizada");
                redirectAttributes.addFlashAttribute("toastType", "success");
            } else {
                response.put("success", false);
                response.put("message", "No se encontró la suscripción");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/reporte")
    public String reporteSuscripciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(defaultValue = "fechaInicio") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            HttpSession session) {

        // Validar admin
        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        // Preparar Pageable
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // ✅ Obtener suscripciones con filtros (el service hace la conversión)
        Page<Suscripcion> suscripcionesPage = suscripcionService.buscarConFiltrosAdmin(
                estado, plan, usuario, fechaInicio, fechaFin, pageable);

        // Estadísticas del reporte
        Map<String, Object> reporteStats = suscripcionService.obtenerEstadisticasReporte();

        // Agregar al modelo
        model.addAttribute("suscripciones", suscripcionesPage.getContent());
        model.addAttribute("currentPage", suscripcionesPage.getNumber());
        model.addAttribute("totalPages", suscripcionesPage.getTotalPages());
        model.addAttribute("totalElements", suscripcionesPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Mantener filtros
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroPlan", plan);
        model.addAttribute("filtroUsuario", usuario);
        model.addAttribute("filtroFechaInicio", fechaInicio);
        model.addAttribute("filtroFechaFin", fechaFin);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("reporteStats", reporteStats);
        model.addAttribute("activePage", "reporte-suscripciones");

        return "admin/reporte-suscripciones";
    }
    @GetMapping("/reporte/exportar-excel")
    public void exportarExcel(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String plan,
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            HttpServletResponse response) throws IOException {

        // Obtener todas las suscripciones con los filtros (sin paginación)
        List<Suscripcion> suscripciones = suscripcionRepository.buscarSinPaginado(
                estado != null && !estado.isEmpty() ? Suscripcion.EstadoSuscripcion.valueOf(estado) : null,
                plan,
                usuario,
                fechaInicio != null && !fechaInicio.isEmpty() ? LocalDate.parse(fechaInicio) : null,
                fechaFin != null && !fechaFin.isEmpty() ? LocalDate.parse(fechaFin) : null
        );

        // Configurar respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_suscripciones.xlsx");

        // Crear workbook y hoja
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Suscripciones");

        // Crear estilos
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);

        // Crear header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Usuario", "Email", "Plan", "Fecha Inicio", "Fecha Fin",
                "Monto Pagado", "Método Pago", "Estado", "Transacción"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Crear filas de datos
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Suscripcion s : suscripciones) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(s.getUsuario().getNombre());
            row.createCell(1).setCellValue(s.getUsuario().getEmail());
            row.createCell(2).setCellValue(s.getPlan().getNombre());
            row.createCell(3).setCellValue(s.getFechaInicio().format(formatter));
            row.createCell(4).setCellValue(s.getFechaFin().format(formatter));
            row.createCell(5).setCellValue(s.getPrecioPagado().doubleValue());
            row.createCell(6).setCellValue(s.getMetodoPago() != null ? s.getMetodoPago().name() : "");
            row.createCell(7).setCellValue(s.getEstado().name());
            row.createCell(8).setCellValue(s.getNumeroTransaccion());

            // Aplicar estilo a todas las celdas
            for (int i = 0; i < row.getLastCellNum(); i++) {
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        // Auto-ajustar columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Escribir en response
        try (FileOutputStream outputStream = new FileOutputStream(java.io.FileDescriptor.out)) {
            workbook.write(response.getOutputStream());
            workbook.close();
        }
    }

}