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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private ICategoriaService categoriaService;

    // ✅ LISTAR CATEGORÍAS DEL USUARIO (CLIENTE)
    @GetMapping("/mis-categorias")
    public String listarMisCategorias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nombre,
            Model model,
            HttpSession session) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Categoria> categoriasPage = categoriaService.listarConPaginacion(usuarioId, pageable);

        model.addAttribute("categorias", categoriasPage.getContent());
        model.addAttribute("currentPage", categoriasPage.getNumber());
        model.addAttribute("totalPages", categoriasPage.getTotalPages());
        model.addAttribute("totalElements", categoriasPage.getTotalElements());
        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("activePage", "categorias");

        return "client/categorias";
    }

    @PostMapping("/categoria/guardar")
    @ResponseBody
    public ResponseEntity<?> guardarCategoria(
            @RequestParam(required = false) Integer id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String color,
            HttpSession session) {

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return ResponseEntity.status(401).body("❌ Sesión expirada");
        }

        try {
            // Validar nombre único
            if (categoriaService.existeNombre(usuarioId, nombre, id)) {
                return ResponseEntity.badRequest().body("❌ Ya existe una categoría con ese nombre");
            }

            Categoria categoria = new Categoria();
            categoria.setId(id);
            categoria.setNombre(nombre);
            categoria.setDescripcion(descripcion);
            categoria.setColor(color != null ? color : "#1565C0");

            // ✅ CORRECTO: Obtener referencia del usuario sin hacer query a BD
            Usuario usuario = new Usuario();
            usuario.setId(usuarioId);
            categoria.setUsuario(usuario);

            if (id != null && id > 0) {
                // Editar existente
                categoria = categoriaService.actualizar(id, categoria);
            } else {
                // Crear nueva
                categoria = categoriaService.crear(categoria);
            }

            return ResponseEntity.ok(categoria);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // ✅ ELIMINAR CATEGORÍA (Soft delete)
    @PostMapping("/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarCategoria(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
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

    // ✅ ACTUALIZAR CATEGORÍA (AJAX)
    @PutMapping("/{id}/actualizar")
    @ResponseBody
    public Map<String, Object> actualizarCategoria(
            @PathVariable Integer id,
            @RequestBody Categoria categoriaActualizada,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        Integer usuarioId = (Integer) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "No autorizado");
            return response;
        }

        try {
            Categoria categoriaActualizadaConId = categoriaService.actualizar(id, categoriaActualizada);
            response.put("success", true);
            response.put("message", "Categoría actualizada correctamente");
            response.put("categoria", categoriaActualizadaConId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }
}