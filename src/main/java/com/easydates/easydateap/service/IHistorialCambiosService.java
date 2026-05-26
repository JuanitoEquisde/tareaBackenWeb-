package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.HistorialCambios;

import java.util.List;

public interface IHistorialCambiosService {

    List<HistorialCambios> findAll();

    List<HistorialCambios> findByTareaId(Integer tareaId);

    HistorialCambios findById(Integer id);

    HistorialCambios save(HistorialCambios historial);

    void deleteById(Integer id);

    long count();

    List<HistorialCambios> searchByAccion(String accion);
}