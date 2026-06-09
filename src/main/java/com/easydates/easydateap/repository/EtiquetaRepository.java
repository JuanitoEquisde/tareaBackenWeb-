package com.easydates.easydateap.repository;

import com.easydates.easydateap.model.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Integer> {
    // Métodos adicionales si los necesitas
}