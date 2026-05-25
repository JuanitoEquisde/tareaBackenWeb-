package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Etiqueta;
import java.util.List;

public interface IEtiquetaService {
    Etiqueta crearEtiqueta(String nombre, String color);
    List<Etiqueta> listarTodas();
    Etiqueta actualizarEtiqueta(Integer id, String nombre, String color);
    boolean eliminarEtiqueta(Integer id);
}