package com.easydates.easydateapp.service;

import com.easydates.easydateapp.entity.Categoria;
import java.util.List;

public interface ICategoriaService {
    Categoria crearCategoria(String nombre, String descripcion, String color, Integer usuarioId);
    List<Categoria> listarPorUsuario(Integer usuarioId);
    Categoria actualizarCategoria(Integer id, String nombre, String descripcion, String color);
    boolean eliminarCategoria(Integer id);
}