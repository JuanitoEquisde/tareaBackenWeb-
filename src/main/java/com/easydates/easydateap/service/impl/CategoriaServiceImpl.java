package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.entity.Categoria;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.CategoriaRepository;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.ICategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ✅ ADMIN: Listar con filtros y paginación
    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarConFiltrosAdmin(String nombre, String estado, String usuario, Pageable pageable) {
        return categoriaRepository.buscarConFiltrosAdmin(nombre, estado, usuario, pageable);
    }

    // ✅ CLIENTE: Listar categorías del usuario
    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarPorUsuario(Integer usuarioId) {
        return categoriaRepository.findActivasGlobalesOPersonales(usuarioId);
    }



    // ✅ Actualizar categoría existente
    @Override
    public Categoria actualizar(Integer id, Categoria categoriaActualizada) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNombre(categoriaActualizada.getNombre());
            categoria.setDescripcion(categoriaActualizada.getDescripcion());
            categoria.setColor(categoriaActualizada.getColor());
            categoria.setEstado(categoriaActualizada.getEstado());
            categoria.setFechaActualizacion(LocalDateTime.now());
            return categoriaRepository.save(categoria);
        }).orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    // ✅ Eliminar permanente
    @Override
    public boolean eliminar(Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Eliminar lógico (desactivar)
    @Override
    public boolean eliminarLogico(Integer id) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setEstado("INACTIVO");
            categoria.setFechaActualizacion(LocalDateTime.now());
            categoriaRepository.save(categoria);
            return true;
        }).orElse(false);
    }

    // ✅ Restaurar categoría
    @Override
    public boolean restaurar(Integer id) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setEstado("ACTIVO");
            categoria.setFechaActualizacion(LocalDateTime.now());
            categoriaRepository.save(categoria);
            return true;
        }).orElse(false);
    }

    // ✅ Buscar por ID
    @Override
    @Transactional(readOnly = true)
    public Categoria findById(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    // ✅ Estadísticas
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", contarTotal());
        stats.put("activas", contarActivas());
        stats.put("inactivas", contarInactivas());
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarTotal() {
        return categoriaRepository.contarActivas();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarActivas() {
        return categoriaRepository.contarActivasEstado();
    }

    @Override
    @Transactional(readOnly = true)
    public Long contarInactivas() {
        return categoriaRepository.contarInactivas();
    }
    @Override
    public Categoria guardar(Categoria categoria) {
        categoria.setFechaCreacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }
}