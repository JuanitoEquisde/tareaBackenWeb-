package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.dto.DashboardStats;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.RolRepository;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    // 🔹 MÉTODOS EXISTENTES (NO MODIFICAR - Ya funcionan)
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> login(String email, String password) {
        System.out.println("🔍 Email: " + email);
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailWithRol(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if ("ELIMINADO".equals(usuario.getEstado())) {
                System.out.println("❌ Login rechazado: usuario eliminado del sistema");
                return Optional.empty();
            }
            if (passwordEncoder.matches(password, usuario.getPassword()) && "ACTIVO".equals(usuario.getEstado())) {
                return Optional.of(usuario);
            }
        }
        System.out.println("❌ Credenciales incorrectas o usuario inactivo");
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        return usuarioRepository.save(usuario);
    }

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
            if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().trim().isEmpty()) {
                usuario.setNombre(usuarioActualizado.getNombre().trim());
            }
            if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().trim().isEmpty()) {
                usuario.setEmail(usuarioActualizado.getEmail().trim());
            }
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }
            if (usuarioActualizado.getEstado() != null && !usuarioActualizado.getEstado().trim().isEmpty()) {
                usuario.setEstado(usuarioActualizado.getEstado().trim());
            }
            if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getId() != null) {
                rolRepository.findById(usuarioActualizado.getRol().getId()).ifPresent(usuario::setRol);
            }
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public boolean eliminarLogico(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado("INACTIVO");
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

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

    @Override
    @Transactional
    public boolean eliminarDelSistema(Integer id, String nombreAdmin) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado("ELIMINADO");
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }

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

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarConFiltros(Integer id, String nombre, String email, String estado, String rol) {
        return usuarioRepository.findAll().stream()
                .filter(u -> u != null && !"ELIMINADO".equals(u.getEstado()))
                .filter(u -> id == null || (u.getId() != null && u.getId().equals(id)))
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

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> buscarConFiltrosOr(String nombre, String email, String estado, String rol) {
        return usuarioRepository.findAll().stream()
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

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> buscarUsuariosPaginados(Integer id, String nombre, String email, String estado, Pageable pageable) {
        Integer idBusqueda = (id != null && id > 0) ? id : null;
        String nombreBusqueda = (nombre != null && !nombre.trim().isEmpty()) ? nombre.trim() : null;
        String emailBusqueda = (email != null && !email.trim().isEmpty()) ? email.trim() : null;
        String estadoBusqueda = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;

        return usuarioRepository.buscarConFiltrosPaginados(
                idBusqueda, nombreBusqueda, emailBusqueda, estadoBusqueda, null, pageable
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();
        List<Usuario> todos = usuarioRepository.findAll().stream()
                .filter(u -> !"ELIMINADO".equals(u.getEstado()))
                .collect(Collectors.toList());

        stats.put("totalUsuarios", todos.size());
        stats.put("usuariosActivos", todos.stream().filter(u -> "ACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("usuariosInactivos", todos.stream().filter(u -> "INACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("totalAdmins", todos.stream().filter(Usuario::isAdmin).count());
        stats.put("usuariosEstandar", todos.stream().filter(Usuario::isUsuarioEstandar).count());
        stats.put("usuariosEliminados", usuarioRepository.findAll().stream()
                .filter(u -> "ELIMINADO".equals(u.getEstado())).count());

        return stats;
    }

    @Override
    public void actualizarUltimoAcceso(Integer usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
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
        if (usuario.getPassword() != null && !usuario.getPassword().startsWith("$2a$")) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        if (usuario.getRol() != null && usuario.getRol().getId() != null) {
            rolRepository.findById(usuario.getRol().getId()).ifPresent(usuario::setRol);
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public void asignarRolPorId(Usuario usuario, Integer rolId) {
        rolRepository.findById(rolId).ifPresent(usuario::setRol);
    }

    // =====================================================
    // ✅ NUEVO: Implementación de obtenerDashboardStats()
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public DashboardStats obtenerDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // Total de usuarios no eliminados
        stats.setTotalUsuarios(usuarioRepository.countActivosNoEliminados());

        // Usuarios por estado
        stats.setUsuariosActivos(usuarioRepository.countByEstado("ACTIVO"));
        stats.setUsuariosInactivos(usuarioRepository.countByEstado("INACTIVO"));

        // Administradores (rol = ADMINISTRADOR y no eliminados)
        stats.setTotalAdmins(usuarioRepository.countByRolNombre("ADMINISTRADOR"));

        return stats;
    }
}