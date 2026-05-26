package com.easydates.easydateap.service;

import com.easydates.easydateap.entity.Usuario;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IUsuarioService {

    Optional<Usuario> login(String email, String password);
    Optional<Usuario> findByEmail(String email);
    Usuario guardar(Usuario usuario);

    Optional<Usuario> findById(Integer id);
    List<Usuario> findAll();
    Usuario actualizar(Integer id, Usuario usuarioActualizado);

    boolean eliminarLogico(Integer id);

    boolean eliminarPermanente(Integer id);

    boolean asignarRol(Integer usuarioId, Integer rolId);
    boolean cambiarEstado(Integer usuarioId, String estado);

    // Si se proporciona cualquier filtro, devuelve usuarios que coincidan con AL MENOS UNO
    List<Usuario> buscarConFiltrosOr(String nombre, String email, String estado, String rol);

    Map<String, Object> obtenerEstadisticas();

    void actualizarUltimoAcceso(Integer usuarioId);
    boolean existeEmail(String email, Integer excluirId);
}