package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.HistorialCambios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IHistorialCambiosService {

    List<HistorialCambios> findAll();

    List<HistorialCambios> findByTareaId(Integer tareaId);

    HistorialCambios findById(Integer id);

    HistorialCambios save(HistorialCambios historial);

    void deleteById(Integer id);
    Page<HistorialCambios> buscarHistorialPaginado(String accion, Pageable pageable);
    long count();

    List<HistorialCambios> searchByAccion(String accion);
}