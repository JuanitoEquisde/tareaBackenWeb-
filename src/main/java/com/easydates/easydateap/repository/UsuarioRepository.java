package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.email = :email")
    Optional<Usuario> findByEmailWithRol(@Param("email") String email);

    boolean existsByEmail(String email);

    // ✅ Buscar usuarios con filtros (AND) - PARA PAGINACIÓN DEL SERVIDOR
    @Query("SELECT u FROM Usuario u JOIN u.rol r WHERE " +
            "(:id IS NULL OR u.id = :id) AND " +
            "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:estado IS NULL OR u.estado = :estado) AND " +
            "(:rol IS NULL OR r.nombre = :rol) AND " +
            "u.estado != 'ELIMINADO'")
    Page<Usuario> buscarConFiltrosPaginados(
            @Param("id") Integer id,
            @Param("nombre") String nombre,
            @Param("email") String email,
            @Param("estado") String estado,
            @Param("rol") String rol,
            Pageable pageable
    );

    // ✅ Contar total de usuarios activos (para estadísticas)
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado != 'ELIMINADO'")
    Long contarActivos();

    // Estadísticas para dashboard admin
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACTIVO'")
    Long contarUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'INACTIVO'")
    Long contarUsuariosInactivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = 'ADMINISTRADOR'")
    Long contarAdmins();

    // Usuarios recientes (para dashboard)
    @Query("SELECT u FROM Usuario u ORDER BY u.id DESC")
    List<Usuario> findUltimosUsuarios(Pageable pageable);

    /**
     * Contar usuarios por estado (ACTIVO/INACTIVO/ELIMINADO)
     */
    long countByEstado(String estado);

    /**
     * Contar usuarios por nombre de rol (ej: "ADMINISTRADOR")
     * Usa JOIN con la entidad Rol para filtrar por nombre
     */
    @Query("SELECT COUNT(u) FROM Usuario u JOIN u.rol r WHERE r.nombre = :nombreRol AND u.estado != 'ELIMINADO'")
    long countByRolNombre(@Param("nombreRol") String nombreRol);

    /**
     * Obtener los 5 usuarios más recientes ordenados por ID descendente
     */
    @Query("SELECT u FROM Usuario u WHERE u.estado != 'ELIMINADO' ORDER BY u.id DESC LIMIT 5")
    List<Usuario> findTop5ByOrderByIdDesc();

    /**
     * Contar total de usuarios no eliminados
     */
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado != 'ELIMINADO'")
    long countActivosNoEliminados();
}