package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Usuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUsuarioService {

    // 🔐 AUTH & REGISTRO
    Optional<Usuario> login(String email, String password);
    Optional<Usuario> findByEmail(String email);
    Usuario guardar(Usuario usuario);

    // 👤 CONSULTAS BÁSICAS
    Optional<Usuario> findById(Integer id);
    List<Usuario> findAll();
    Usuario actualizar(Integer id, Usuario usuarioActualizado);

    // ✏️ GESTIÓN
    boolean asignarRol(Integer usuarioId, Integer rolId);
    boolean cambiarEstado(Integer usuarioId, String estado);

    // 🗑️ ELIMINACIÓN (3 TIPOS)
    boolean eliminarLogico(Integer id);                    // → Estado: INACTIVO
    boolean eliminarPermanente(Integer id);                // → Borrado físico de BD
    boolean eliminarDelSistema(Integer id, String nombreAdmin);  // → Estado: ELIMINADO
    boolean restaurarUsuario(Integer id);                  // → ELIMINADO → ACTIVO
    List<Usuario> listarEliminados();                      // Solo usuarios con estado ELIMINADO

    // 🔍 BÚSQUEDAS CON FILTROS (excluyen automáticamente "ELIMINADO")
    List<Usuario> buscarConFiltros(String nombre, String email, String estado, String rol);  // AND
    List<Usuario> buscarConFiltrosOr(String nombre, String email, String estado, String rol); // OR

    // 📊 ESTADÍSTICAS Y UTILIDADES
    Map<String, Object> obtenerEstadisticas();
    void actualizarUltimoAcceso(Integer usuarioId);
    boolean existeEmail(String email, Integer excluirId);

    Usuario crearUsuario(Usuario usuario);
    void asignarRolPorId(Usuario usuario, Integer rolId);
}