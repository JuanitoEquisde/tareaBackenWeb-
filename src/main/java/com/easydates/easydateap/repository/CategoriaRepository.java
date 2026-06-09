package com.easydates.easydateap.repository;

import com.easydates.easydateap.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // ✅ Listar categorías por usuario (activas)
    List<Categoria> findByUsuarioIdAndEstadoOrderByNombre(Integer usuarioId, String estado);

    // ✅ Buscar con filtros simples
    @Query("SELECT c FROM Categoria c WHERE c.usuario.id = :usuarioId " +
            "AND (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND c.estado <> 'ELIMINADA' " +
            "ORDER BY c.nombre")
    List<Categoria> buscarPorUsuarioYNombre(@Param("usuarioId") Integer usuarioId,
                                            @Param("nombre") String nombre);

    // ✅ Paginación simple
    @Query("SELECT c FROM Categoria c WHERE c.usuario.id = :usuarioId AND c.estado <> 'ELIMINADA' ORDER BY c.nombre")
    Page<Categoria> findByUsuarioIdAndEstadoNot(@Param("usuarioId") Integer usuarioId,
                                                Pageable pageable);

    // ✅ Contar categorías activas por usuario
    Long countByUsuarioIdAndEstado(Integer usuarioId, String estado);

    // ✅ Verificar si existe nombre duplicado
    boolean existsByUsuarioIdAndNombreAndEstadoNot(Integer usuarioId, String nombre, String estado);

    // ✅ Eliminar lógico por usuario (SOFT DELETE - Cambia estado a ELIMINADA)
    @Modifying
    @Transactional
    @Query("UPDATE Categoria c SET c.estado = 'ELIMINADA' WHERE c.usuario.id = :usuarioId")
    void eliminarLogicoPorUsuarioId(@Param("usuarioId") Integer usuarioId);

    // ✅ Eliminar físico por usuario (HARD DELETE - Borra permanentemente)
    @Modifying
    @Transactional
    @Query("DELETE FROM Categoria c WHERE c.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // ✅ Buscar por ID y usuario
    Optional<Categoria> findByIdAndUsuarioId(Integer id, Integer usuarioId);
}