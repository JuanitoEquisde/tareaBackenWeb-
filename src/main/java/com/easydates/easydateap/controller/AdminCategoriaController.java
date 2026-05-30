package com.easydates.easydateap.controller;

import com.easydates.easydateap.entity.Categoria;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.UsuarioRepository;
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

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/categorias")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminCategoriaController {

    @Autowired
    private ICategoriaService categoriaService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    // LISTAR con filtros y paginación
    @GetMapping
    public String listarCategorias(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String usuario,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model,
            HttpSession session) {

        Usuario admin = (Usuario) session.getAttribute("usuario");
        if (admin == null || !admin.isAdmin()) {
            return "redirect:/login";
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Categoria> paginaCategorias = categoriaService.listarConFiltrosAdmin(nombre, estado, usuario, pageable);

        model.addAttribute("categorias", paginaCategorias.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", paginaCategorias.getTotalPages());
        model.addAttribute("totalElements", paginaCategorias.getTotalElements());
        model.addAttribute("pageSize", size);

        model.addAttribute("filtroNombre", nombre);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroUsuario", usuario);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("stats", categoriaService.obtenerEstadisticas());
        model.addAttribute("activePage", "categorias");

        return "admin/categorias";
    }

    // CREAR categoría
    @PostMapping("/crear")
    @ResponseBody
    public Map<String, Object> crearCategoria(@RequestBody Map<String, Object> categoriaData, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Categoria categoria = new Categoria();
            categoria.setNombre((String) categoriaData.get("nombre"));
            categoria.setDescripcion((String) categoriaData.get("descripcion"));
            categoria.setColor((String) categoriaData.get("color"));
            categoria.setEstado("ACTIVO");

            // Asignar usuario si se proporciona
            Integer usuarioId = null;
            if (categoriaData.get("usuarioId") != null) {
                usuarioId = Integer.parseInt(categoriaData.get("usuarioId").toString());
                Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
                if (usuario != null) {
                    categoria.setUsuario(usuario);
                }
            }

            categoriaService.guardar(categoria);
            response.put("success", true);
            response.put("message", "Categoría creada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ ACTUALIZAR categoría
    @PutMapping("/{id}/actualizar")
    @ResponseBody
    public Map<String, Object> actualizarCategoria(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> categoriaData) {
        Map<String, Object> response = new HashMap<>();
        try {
            Categoria categoria = categoriaService.findById(id);
            if (categoria == null) {
                response.put("success", false);
                response.put("message", "Categoría no encontrada");
                return response;
            }

            categoria.setNombre((String) categoriaData.get("nombre"));
            categoria.setDescripcion((String) categoriaData.get("descripcion"));
            categoria.setColor((String) categoriaData.get("color"));
            categoria.setEstado((String) categoriaData.get("estado"));

            categoriaService.actualizar(id, categoria);
            response.put("success", true);
            response.put("message", "Categoría actualizada correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ ELIMINAR lógico (desactivar)
    @DeleteMapping("/{id}/eliminar")
    @ResponseBody
    public Map<String, Object> eliminarCategoria(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exito = categoriaService.eliminarLogico(id);
            response.put("success", exito);
            response.put("message", exito ? "Categoría desactivada" : "Error al desactivar");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ RESTAURAR categoría
    @PostMapping("/{id}/restaurar")
    @ResponseBody
    public Map<String, Object> restaurarCategoria(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exito = categoriaService.restaurar(id);
            response.put("success", exito);
            response.put("message", exito ? "Categoría restaurada" : "Error al restaurar");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ ELIMINAR PERMANENTE
    @PostMapping("/{id}/eliminar-permanente")
    @ResponseBody
    public Map<String, Object> eliminarPermanente(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean exito = categoriaService.eliminar(id);
            response.put("success", exito);
            response.put("message", exito ? "Categoría eliminada permanentemente" : "Error al eliminar");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
        }
        return response;
    }

    // ✅ OBTENER categoría por ID (para modal de edición)
    @GetMapping("/{id}")
    @ResponseBody
    public Map<String, Object> obtenerCategoria(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Categoria categoria = categoriaService.findById(id);
            if (categoria != null) {
                response.put("success", true);
                response.put("categoria", categoria);
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
}