package com.easydates.easydateapp.repository;

import com.easydates.easydateapp.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    @Query("SELECT c FROM Categoria c WHERE c.estado = 'ACTIVO' AND (c.usuario IS NULL OR c.usuario.id = :usuarioId) ORDER BY c.nombre")
    List<Categoria> findActivasGlobalesOPersonales(@Param("usuarioId") Integer usuarioId);
}