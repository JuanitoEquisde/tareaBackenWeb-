package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.entity.Categoria;
import com.easydates.easydateap.repository.CategoriaRepository;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.ICategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoriaServiceImpl implements ICategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public Categoria crearCategoria(String nombre, String descripcion, String color, Integer usuarioId) {
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        categoria.setColor(color != null ? color : "#1565C0");
        categoria.setEstado("ACTIVO");

        usuarioRepository.findById(usuarioId).ifPresent(categoria::setUsuario);

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarPorUsuario(Integer usuarioId) {
        System.out.println("🔍 [DEBUG] Buscando categorías para usuario: " + usuarioId);
        // ✅ Usamos la nueva consulta que incluye globales
        List<Categoria> resultado = categoriaRepository.findActivasGlobalesOPersonales(usuarioId);
        System.out.println("✅ [DEBUG] Encontradas: " + resultado.size() + " categorías");
        return resultado;
    }

    @Override
    public Categoria actualizarCategoria(Integer id, String nombre, String descripcion, String color) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        categoria.setNombre(nombre);
        categoria.setDescripcion(descripcion);
        if (color != null) {
            categoria.setColor(color);
        }

        return categoriaRepository.save(categoria);
    }

    @Override
    public boolean eliminarCategoria(Integer id) {
        return categoriaRepository.findById(id).map(categoria -> {
            categoria.setEstado("ELIMINADO");
            categoriaRepository.save(categoria);
            return true;
        }).orElse(false);
    }
}