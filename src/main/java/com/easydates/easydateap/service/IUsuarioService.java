package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Usuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUsuarioService {

    // ✅ Métodos existentes (NO modificar)
    Optional<Usuario> login(String email, String password);
    Optional<Usuario> findByEmail(String email);
    Usuario guardar(Usuario usuario);

    // ✅ NUEVOS: Para administración
    Optional<Usuario> findById(Integer id);
    List<Usuario> findAll();
    Usuario actualizar(Integer id, Usuario usuarioActualizado);
    boolean eliminarLogico(Integer id);

    // ✅ Gestión de roles y estado
    boolean asignarRol(Integer usuarioId, Integer rolId);
    boolean cambiarEstado(Integer usuarioId, String estado);

    // ✅ Búsqueda con filtros (para tabla de usuarios admin)
    List<Usuario> buscarConFiltros(String nombre, String email, String estado, String rol);

    // ✅ Estadísticas para dashboard admin
    Map<String, Object> obtenerEstadisticas();

    // ✅ Utilidades
    void actualizarUltimoAcceso(Integer usuarioId);
    boolean existeEmail(String email, Integer excluirId);
}