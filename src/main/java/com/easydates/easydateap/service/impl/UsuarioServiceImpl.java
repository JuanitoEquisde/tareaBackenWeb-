package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.RolRepository;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =====================================================
    // LOGIN - ✅ CORREGIDO: Rechaza usuarios ELIMINADOS
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String email, String password) {
        System.out.println("🔍 Email: " + email);
        System.out.println("🔍 Password ingresada: '" + password + "'");

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailWithRol(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String passwordEnBD = usuario.getPassword();

            System.out.println("🔍 Password en BD: '" + passwordEnBD + "'");
            System.out.println("🔍 ¿Coincide? " + passwordEncoder.matches(password, passwordEnBD));

            // ✅ RECHAZAR login si el usuario está eliminado del sistema
            if ("ELIMINADO".equals(usuario.getEstado())) {
                System.out.println("❌ Login rechazado: usuario eliminado del sistema");
                return Optional.empty();
            }

            if (passwordEncoder.matches(password, passwordEnBD) && "ACTIVO".equals(usuario.getEstado())) {
                System.out.println("🔍 [DEBUG] Rol del usuario: " +
                        (usuario.getRol() != null ? usuario.getRol().getNombre() : "NULL"));
                return Optional.of(usuario);
            }
        }

        System.out.println("❌ Credenciales incorrectas o usuario inactivo");
        return Optional.empty();
    }

    // =====================================================
    // MÉTODOS EXISTENTES
    // =====================================================
    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        // Encriptar password solo si es nuevo o no está encriptado
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        // Estado por defecto
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        return usuarioRepository.save(usuario);
    }

    // =====================================================
    // MÉTODOS PARA ADMINISTRADOR
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> findById(Integer id) {
        return usuarioRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario actualizar(Integer id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            // Nombre
            if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().trim().isEmpty()) {
                usuario.setNombre(usuarioActualizado.getNombre().trim());
            }
            // Email
            if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().trim().isEmpty()) {
                usuario.setEmail(usuarioActualizado.getEmail().trim());
            }
            // Password (solo si se proporciona uno nuevo)
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }
            // Estado
            if (usuarioActualizado.getEstado() != null && !usuarioActualizado.getEstado().trim().isEmpty()) {
                usuario.setEstado(usuarioActualizado.getEstado().trim());
            }
            // Rol (solo si tiene ID válido)
            if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getId() != null) {
                rolRepository.findById(usuarioActualizado.getRol().getId())
                        .ifPresent(usuario::setRol);
            }
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    // ✅ Eliminación lógica tradicional (INACTIVO)
    @Override
    public boolean eliminarLogico(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado("INACTIVO");
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

    // ✅ Eliminación permanente (Hard Delete) - BORRADO FÍSICO DE LA BD
    @Override
    public boolean eliminarPermanente(Integer id) {
        try {
            if (usuarioRepository.existsById(id)) {
                usuarioRepository.deleteById(id);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar permanentemente: " + e.getMessage());
            return false;
        }
    }

    // ✅ NUEVO: Eliminación del sistema (Soft Delete Avanzado)
    // El usuario queda con estado "ELIMINADO": no aparece en listados, no puede login, pero sigue en BD
    @Override
    @Transactional
    public boolean eliminarDelSistema(Integer id, String nombreAdmin) {
        return usuarioRepository.findById(id).map(usuario -> {

            // Cambiar estado a "ELIMINADO"
            usuario.setEstado("ELIMINADO");
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

    // ✅ NUEVO: Restaurar usuario eliminado del sistema
    @Override
    @Transactional
    public boolean restaurarUsuario(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            if ("ELIMINADO".equals(usuario.getEstado())) {
                usuario.setEstado("ACTIVO");
                usuarioRepository.save(usuario);
                return true;
            }
            return false;
        }).orElse(false);
    }

    // ✅ NUEVO: Listar usuarios eliminados del sistema (solo para admin)
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarEliminados() {
        return usuarioRepository.findAll().stream()
                .filter(u -> "ELIMINADO".equals(u.getEstado()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean asignarRol(Integer usuarioId, Integer rolId) {
        return usuarioRepository.findById(usuarioId).map(usuario -> {
            rolRepository.findById(rolId).ifPresent(usuario::setRol);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean cambiarEstado(Integer usuarioId, String estado) {
        return usuarioRepository.findById(usuarioId).map(usuario -> {
            usuario.setEstado(estado);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

    // =====================================================
    // BÚSQUEDAS CON FILTROS
    // =====================================================

    //Búsqueda con lógica AND (todos los filtros deben coincidir)

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarConFiltros(String nombre, String email, String estado, String rol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u != null && !"ELIMINADO".equals(u.getEstado()))
                .filter(u -> nombre == null || nombre.trim().isEmpty() ||
                        (u.getNombre() != null && u.getNombre().toLowerCase().contains(nombre.toLowerCase().trim())))
                .filter(u -> email == null || email.trim().isEmpty() ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(email.toLowerCase().trim())))
                .filter(u -> estado == null || estado.trim().isEmpty() ||
                        (u.getEstado() != null && u.getEstado().equalsIgnoreCase(estado.trim())))
                .filter(u -> rol == null || rol.trim().isEmpty() ||
                        (u.getRol() != null && u.getRol().getNombre() != null &&
                                u.getRol().getNombre().equalsIgnoreCase(rol.trim())))
                .collect(Collectors.toList());
    }

    // Búsqueda con lógica OR (cualquier filtro que coincida) - EXCLUYE ELIMINADOS
    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarConFiltrosOr(String nombre, String email, String estado, String rol) {

        // Obtener todos los usuarios y filtrar en memoria
        return usuarioRepository.findAll().stream()
                // ✅ Siempre excluir usuarios eliminados del sistema
                .filter(u -> u != null && !"ELIMINADO".equals(u.getEstado()))
                .filter(u -> {
                    boolean coincideNombre = (nombre == null || nombre.trim().isEmpty()) ||
                            (u.getNombre() != null && u.getNombre().toLowerCase().contains(nombre.toLowerCase().trim()));

                    boolean coincideEmail = (email == null || email.trim().isEmpty()) ||
                            (u.getEmail() != null && u.getEmail().toLowerCase().contains(email.toLowerCase().trim()));

                    boolean coincideEstado = (estado == null || estado.trim().isEmpty()) ||
                            (u.getEstado() != null && u.getEstado().equalsIgnoreCase(estado.trim()));

                    boolean coincideRol = (rol == null || rol.trim().isEmpty()) ||
                            (u.getRol() != null && u.getRol().getNombre() != null &&
                                    u.getRol().getNombre().equalsIgnoreCase(rol.trim()));
                    return coincideNombre || coincideEmail || coincideEstado || coincideRol;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarConFiltrosOrPuro(String nombre, String email, String estado, String rol) {
        List<Usuario> todos = usuarioRepository.findAll().stream()
                .filter(u -> !"ELIMINADO".equals(u.getEstado()))
                .collect(Collectors.toList());

        // Si no hay filtros, retornar todos excepto eliminados
        if (nombre == null && email == null && estado == null && rol == null) {
            return todos;
        }

        return todos.stream()
                .filter(u -> {
                    boolean coincideNombre = (nombre == null || nombre.isEmpty()) ||
                            u.getNombre().toLowerCase().contains(nombre.toLowerCase());
                    boolean coincideEmail = (email == null || email.isEmpty()) ||
                            u.getEmail().toLowerCase().contains(email.toLowerCase());
                    boolean coincideEstado = (estado == null || estado.isEmpty()) ||
                            u.getEstado().equalsIgnoreCase(estado);
                    boolean coincideRol = (rol == null || rol.isEmpty()) ||
                            (u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol));

                    //  LÓGICA OR: Retorna true si AL MENOS UN filtro coincide
                    return coincideNombre || coincideEmail || coincideEstado || coincideRol;
                })
                .collect(Collectors.toList());
    }

    // =====================================================
    // ESTADÍSTICAS Y UTILIDADES
    // =====================================================
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        //  Excluir usuarios eliminados de las estadísticas
        List<Usuario> todos = usuarioRepository.findAll().stream()
                .filter(u -> !"ELIMINADO".equals(u.getEstado()))
                .collect(Collectors.toList());

        stats.put("totalUsuarios", todos.size());
        stats.put("usuariosActivos", todos.stream().filter(u -> "ACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("usuariosInactivos", todos.stream().filter(u -> "INACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("totalAdmins", todos.stream().filter(Usuario::isAdmin).count());
        stats.put("usuariosEstandar", todos.stream().filter(Usuario::isUsuarioEstandar).count());
        //  Nuevo: Contar eliminados del sistema
        stats.put("usuariosEliminados", usuarioRepository.findAll().stream()
                .filter(u -> "ELIMINADO".equals(u.getEstado()))
                .count());

        return stats;
    }

    @Override
    public void actualizarUltimoAcceso(Integer usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            // Si tienes el campo ultimoAcceso en tu entidad, descomenta:
            // usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email, Integer excluirId) {
        return usuarioRepository.findByEmail(email)
                .map(u -> !u.getId().equals(excluirId) && !"ELIMINADO".equals(u.getEstado()))
                .orElse(false);
    }
    @Override
    public Usuario crearUsuario(Usuario usuario) {
        // Encriptar contraseña
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }

        // Estado por defecto
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }

        // Asignar rol
        if (usuario.getRol() != null && usuario.getRol().getId() != null) {
            rolRepository.findById(usuario.getRol().getId())
                    .ifPresent(usuario::setRol);
        }

        return usuarioRepository.save(usuario);
    }
    @Override
    public void asignarRolPorId(Usuario usuario, Integer rolId) {
        rolRepository.findById(rolId).ifPresent(usuario::setRol);
    }
}