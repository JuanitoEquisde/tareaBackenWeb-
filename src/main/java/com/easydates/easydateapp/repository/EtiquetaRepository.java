package com.easydates.easydateapp.repository;

import com.easydates.easydateapp.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
    // Métodos adicionales si los necesitas
}