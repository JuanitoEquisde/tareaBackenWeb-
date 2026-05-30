package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    // Para clientes: categorías globales O personales del usuario
    @Query("SELECT c FROM Categoria c WHERE c.estado = 'ACTIVO' AND (c.usuario IS NULL OR c.usuario.id = :usuarioId) ORDER BY c.nombre")
    List<Categoria> findActivasGlobalesOPersonales(@Param("usuarioId") Integer usuarioId);

    // Para ADMIN: TODAS las categorías con filtros (SIN ORDER BY - Pageable lo maneja)
    @Query("SELECT c FROM Categoria c " +
            "LEFT JOIN c.usuario u " +
            "WHERE (:nombre IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) " +
            "AND (:estado IS NULL OR :estado = '' OR c.estado = :estado) " +  // ← CORREGIDO
            "AND (:usuario IS NULL OR u IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :usuario, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :usuario, '%')))")
    Page<Categoria> buscarConFiltrosAdmin(@Param("nombre") String nombre,
                                          @Param("estado") String estado,
                                          @Param("usuario") String usuario,
                                          Pageable pageable);

    // Contadores para estadísticas
    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.estado <> 'ELIMINADA'")
    Long contarActivas();

    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.estado = 'ACTIVO'")
    Long contarActivasEstado();

    @Query("SELECT COUNT(c) FROM Categoria c WHERE c.estado = 'INACTIVO'")
    Long contarInactivas();

    //Eliminar categorías de un usuario
    @Modifying
    @Query("DELETE FROM Categoria c WHERE c.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Integer usuarioId);

    // listar por usuario
    List<Categoria> findByUsuarioIdOrderByNombre(Integer usuarioId);
}