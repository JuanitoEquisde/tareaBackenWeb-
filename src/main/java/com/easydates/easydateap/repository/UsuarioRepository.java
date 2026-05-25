package com.easydates.easydateap.repository;

import com.easydates.easydateap.entity.Usuario;
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

    //Buscar usuarios con filtros (para admin)
    @Query("SELECT u FROM Usuario u JOIN u.rol r WHERE " +
            "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:estado IS NULL OR u.estado = :estado) AND " +
            "(:rol IS NULL OR r.nombre = :rol)")
    List<Usuario> buscarConFiltros(@Param("nombre") String nombre,
                                   @Param("email") String email,
                                   @Param("estado") String estado,
                                   @Param("rol") String rol);

    // ✅ Estadísticas para dashboard admin
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'ACTIVO'")
    Long contarUsuariosActivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.estado = 'INACTIVO'")
    Long contarUsuariosInactivos();

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol.nombre = 'Administrador'")
    Long contarAdmins();

    // ✅ Usuarios recientes (para dashboard)
    @Query("SELECT u FROM Usuario u ORDER BY u.id DESC")
    List<Usuario> findUltimosUsuarios(org.springframework.data.domain.Pageable pageable);
}
