package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Categoria;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.service.ICategoriaService;
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
import java.util.HashMap;
import java.util.Map;
//ho

@Controller
@RequestMapping("/admin/categorias")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminCategoriaController {
    @Autowired
    private ICategoriaService categoriaService;

    // ✅ LISTAR TODAS LAS CATEGORÍAS (ADMIN)
    @GetMapping
    public String listarCategorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            Model model,
            HttpSession session) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());

        // Para admin: listar categorías de todos los usuarios (o filtrar por usuario si se desea)
        // Aquí listamos las del admin actual, pero puedes modificar para listar todas
        Page<Categoria> categoriasPage = categoriaService.listarConPaginacion(admin.getId(), pageable);

        model.addAttribute("categorias", categoriasPage.getContent());
        model.addAttribute("currentPage", categoriasPage.getNumber());
        model.addAttribute("totalPages", categoriasPage.getTotalPages());
        model.addAttribute("totalElements", categoriasPage.getTotalElements());
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("activePage", "categorias");

        return "admin/categorias";
    }

    // ✅ GUARDAR CATEGORÍA (CREAR O EDITAR)
    @PostMapping("/guardar")
    public String guardarCategoria(
            @ModelAttribute Categoria categoria,
            @RequestParam(required = false) Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        try {
            // Validar nombre único para este usuario
            if (categoriaService.existeNombre(admin.getId(), categoria.getNombre(), id)) {
                redirectAttributes.addFlashAttribute("error", "Ya existe una categoría con ese nombre");
                redirectAttributes.addFlashAttribute("categoriaForm", categoria);
                return "redirect:/admin/categorias";
            }

            // Asignar usuario actual a la categoría
            categoria.setUsuario(admin);

            if (id != null && id > 0) {
                // Editar existente
                categoriaService.actualizar(id, categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría actualizada correctamente");
            } else {
                // Crear nueva
                categoriaService.crear(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría creada correctamente");
            }
            redirectAttributes.addFlashAttribute("tipoMensaje", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipoMensaje", "danger");
        }

        return "redirect:/admin/categorias";
    }

    // ✅ ELIMINAR CATEGORÍA (Soft delete)
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarCategoria(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            response.put("success", false);
            response.put("message", "No autorizado");
            return response;
        }

        try {
            boolean exito = categoriaService.eliminar(id);
            if (exito) {
                response.put("success", true);
                response.put("message", "Categoría eliminada correctamente");
            } else {
                response.put("success", false);
                response.put("message", "Categoría no encontrada");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ OBTENER CATEGORÍA POR ID (para editar en modal)
    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> obtenerCategoria(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        categoriaService.obtenerPorId(id).ifPresent(categoria -> {
            response.put("id", categoria.getId());
            response.put("nombre", categoria.getNombre());
            response.put("color", categoria.getColor());
            response.put("descripcion", categoria.getDescripcion());
            response.put("estado", categoria.getEstado());
        });
        return response;
    }
}