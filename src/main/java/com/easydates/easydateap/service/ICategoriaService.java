package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ICategoriaService {

    // ✅ Para ADMIN
    Page<Categoria> listarConFiltrosAdmin(String nombre, String estado, String usuario, Pageable pageable);

    // ✅ Para CLIENTE
    List<Categoria> listarPorUsuario(Integer usuarioId);

    // ✅ CRUD básico
    Categoria guardar(Categoria categoria);
    Categoria actualizar(Integer id, Categoria categoriaActualizada);
    boolean eliminar(Integer id);
    boolean eliminarLogico(Integer id);
    boolean restaurar(Integer id);
    Categoria findById(Integer id);

    // ✅ Estadísticas
    Map<String, Long> obtenerEstadisticas();
    Long contarTotal();
    Long contarActivas();
    Long contarInactivas();
}