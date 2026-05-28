package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.HistorialCambios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialCambiosRepository extends JpaRepository<HistorialCambios, Integer> {

    // Obtener todos los cambios ordenados por fecha (más reciente primero)
    List<HistorialCambios> findAllByOrderByFechaCambioDesc();

    // Obtener cambios de una tarea específica
    List<HistorialCambios> findByTareaIdOrderByFechaCambioDesc(Integer tareaId);

    // Contar total de cambios
    long count();

    // Buscar por acción
    List<HistorialCambios> findByAccionContainingOrderByFechaCambioDesc(String accion);

    @Query("SELECT h FROM HistorialCambios h WHERE " +
            "(:accion IS NULL OR h.accion = :accion) " +
            "ORDER BY h.fechaCambio DESC")
    Page<HistorialCambios> buscarPorAccionPaginado(@Param("accion") String accion, Pageable pageable);

    // Consulta personalizada con JOIN para traer datos de la tarea y usuario
    @Query("SELECT h FROM HistorialCambios h JOIN FETCH h.tarea t ORDER BY h.fechaCambio DESC")
    List<HistorialCambios> findAllWithTarea();
}