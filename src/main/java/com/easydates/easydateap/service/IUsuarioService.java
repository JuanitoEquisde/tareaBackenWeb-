package com.easydates.easydateap.service;

import com.easydates.easydateap.dto.DashboardStats;
import com.easydates.easydateap.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUsuarioService {

    // =====================================================
    // 🔹 MÉTODOS EXISTENTES (NO MODIFICAR - Ya funcionan)
    // =====================================================

    Optional<Usuario> login(String email, String password);

    Optional<Usuario> findByEmail(String email);

    Usuario guardar(Usuario usuario);

    Optional<Usuario> findById(Integer id);

    List<Usuario> findAll();

    Usuario actualizar(Integer id, Usuario usuarioActualizado);

    boolean eliminarLogico(Integer id);

    boolean eliminarPermanente(Integer id);

    boolean eliminarDelSistema(Integer id, String nombreAdmin);

    boolean restaurarUsuario(Integer id);

    List<Usuario> listarEliminados();

    boolean asignarRol(Integer usuarioId, Integer rolId);

    boolean cambiarEstado(Integer usuarioId, String estado);

    List<Usuario> buscarConFiltros(Integer id, String nombre, String email, String estado, String rol);

    List<Usuario> buscarConFiltrosOr(String nombre, String email, String estado, String rol);

    Page<Usuario> buscarUsuariosPaginados(Integer id, String nombre, String email, String estado, Pageable pageable);

    Map<String, Object> obtenerEstadisticas();

    void actualizarUltimoAcceso(Integer usuarioId);

    boolean existeEmail(String email, Integer excluirId);

    Usuario crearUsuario(Usuario usuario);

    void asignarRolPorId(Usuario usuario, Integer rolId);

    // =====================================================
    // ✅ NUEVO: Método para DashboardStats DTO
    // =====================================================

    /**
     * Obtiene estadísticas estructuradas para el dashboard administrativo
     * @return DashboardStats con totalUsuarios, usuariosActivos, usuariosInactivos, totalAdmins
     */
    DashboardStats obtenerDashboardStats();
}