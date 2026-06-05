package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ICategoriaService {

    List<Categoria> listarPorUsuario(Integer usuarioId);

    Page<Categoria> listarConPaginacion(Integer usuarioId, Pageable pageable);

    Optional<Categoria> obtenerPorId(Integer id);

    Categoria crear(Categoria categoria);

    Categoria actualizar(Integer id, Categoria categoriaActualizada);

    boolean eliminar(Integer id);

    boolean existeNombre(Integer usuarioId, String nombre, Integer excluirId);

    void eliminarPorUsuario(Integer usuarioId);
}