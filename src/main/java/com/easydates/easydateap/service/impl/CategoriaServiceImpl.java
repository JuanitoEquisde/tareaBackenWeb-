package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.model.Categoria;
import com.easydates.easydateap.repository.CategoriaRepository;
import com.easydates.easydateap.service.ICategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarPorUsuario(Integer usuarioId) {
        return categoriaRepository.findByUsuarioIdAndEstadoOrderByNombre(usuarioId, "ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Categoria> listarConPaginacion(Integer usuarioId, Pageable pageable) {
        return categoriaRepository.findByUsuarioIdAndEstadoNot(usuarioId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Categoria> obtenerPorId(Integer id) {
        return categoriaRepository.findById(id);
    }

    @Override
    public Categoria crear(Categoria categoria) {
        categoria.setEstado("ACTIVO");
        categoria.setFechaCreacion(LocalDateTime.now());
        categoria.setFechaActualizacion(LocalDateTime.now());
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Integer id, Categoria categoriaActualizada) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setNombre(categoriaActualizada.getNombre());
            categoria.setColor(categoriaActualizada.getColor());
            categoria.setDescripcion(categoriaActualizada.getDescripcion());
            categoria.setFechaActualizacion(LocalDateTime.now());
            return categoriaRepository.save(categoria);
        }).orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
    }

    @Override
    public boolean eliminar(Integer id) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setEstado("ELIMINADA");  // Soft delete
            categoria.setFechaActualizacion(LocalDateTime.now());
            categoriaRepository.save(categoria);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeNombre(Integer usuarioId, String nombre, Integer excluirId) {
        boolean existe = categoriaRepository.existsByUsuarioIdAndNombreAndEstadoNot(usuarioId, nombre, "ELIMINADA");

        // Si existe pero es la misma categoría que estamos editando, no es conflicto
        if (existe && excluirId != null) {
            return categoriaRepository.findById(excluirId)
                    .map(c -> !c.getNombre().equalsIgnoreCase(nombre))
                    .orElse(false);
        }

        return existe;
    }

    @Override
    @Transactional
    public void eliminarPorUsuario(Integer usuarioId) {
        categoriaRepository.eliminarLogicoPorUsuarioId(usuarioId);
    }
}