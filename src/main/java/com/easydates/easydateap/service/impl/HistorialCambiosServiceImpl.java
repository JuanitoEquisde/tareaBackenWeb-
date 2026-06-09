package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.model.HistorialCambios;
import com.easydates.easydateap.repository.HistorialCambiosRepository;
import com.easydates.easydateap.service.IHistorialCambiosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistorialCambiosServiceImpl implements IHistorialCambiosService {

    @Autowired
    private HistorialCambiosRepository historialRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findAll() {
        return historialRepository.findAllByOrderByFechaCambioDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> findByTareaId(Integer tareaId) {
        return historialRepository.findByTareaIdOrderByFechaCambioDesc(tareaId);
    }

    @Override
    @Transactional(readOnly = true)
    public HistorialCambios findById(Integer id) {
        return historialRepository.findById(id).orElse(null);
    }

    @Override
    public HistorialCambios save(HistorialCambios historial) {
        return historialRepository.save(historial);
    }

    @Override
    public void deleteById(Integer id) {
        historialRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return historialRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialCambios> searchByAccion(String accion) {
        return historialRepository.findByAccionContainingOrderByFechaCambioDesc(accion);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistorialCambios> buscarHistorialPaginado(String accion, Pageable pageable) {
        String accionBusqueda = (accion != null && !accion.trim().isEmpty())
                ? accion.trim().toUpperCase()
                : null;
        return historialRepository.buscarPorAccionPaginado(accionBusqueda, pageable);
    }
}