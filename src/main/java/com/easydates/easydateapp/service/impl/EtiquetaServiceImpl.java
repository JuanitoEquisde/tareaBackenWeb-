package com.easydates.easydateapp.service.impl;

import com.easydates.easydateapp.entity.Etiqueta;
import com.easydates.easydateapp.repository.EtiquetaRepository;
import com.easydates.easydateapp.service.IEtiquetaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class EtiquetaServiceImpl implements IEtiquetaService {

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @Override
    public Etiqueta crearEtiqueta(String nombre, String color) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNombre(nombre);
        etiqueta.setColor(color != null ? color : "#6c757d");
        return etiquetaRepository.save(etiqueta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Etiqueta> listarTodas() {
        return etiquetaRepository.findAll();
    }

    @Override
    public Etiqueta actualizarEtiqueta(Integer id, String nombre, String color) {
        Etiqueta etiqueta = etiquetaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));

        etiqueta.setNombre(nombre);
        if (color != null) {
            etiqueta.setColor(color);
        }

        return etiquetaRepository.save(etiqueta);
    }

    @Override
    public boolean eliminarEtiqueta(Integer id) {
        return etiquetaRepository.findById(id).map(etiqueta -> {
            etiquetaRepository.delete(etiqueta);
            return true;
        }).orElse(false);
    }
}