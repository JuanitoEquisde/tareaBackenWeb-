package com.easydates.easydateapp.service.impl;

import com.easydates.easydateapp.entity.Rol;
import com.easydates.easydateapp.entity.Usuario;
import com.easydates.easydateapp.repository.RolRepository;
import com.easydates.easydateapp.repository.UsuarioRepository;
import com.easydates.easydateapp.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    // MÉTODOS EXISTENTES (NO MODIFICAR - Ya funcionan)
    // =====================================================

    @Override
    public Optional<Usuario> login(String email, String password) {
        System.out.println("🔍 Email: " + email);
        System.out.println("🔍 Password ingresada: '" + password + "'");
        System.out.println("🔍 Longitud password: " + (password != null ? password.length() : 0));

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String passwordEnBD = usuario.getPassword();

            System.out.println("🔍 Password en BD: '" + passwordEnBD + "'");
            System.out.println("🔍 Longitud password BD: " + (passwordEnBD != null ? passwordEnBD.length() : 0));
            System.out.println("🔍 Empieza con $2a$10$? " + (passwordEnBD != null ? passwordEnBD.startsWith("$2a$10$") : false));

            boolean matches = passwordEncoder.matches(password, passwordEnBD);
            System.out.println("🔍 ¿Coincide? " + matches);

            return usuarioOpt.filter(u -> matches && "ACTIVO".equals(u.getEstado()));
        }

        System.out.println("❌ Usuario no encontrado");
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        // Encriptar password solo si es nuevo usuario
        if (usuario.getId() == null && usuario.getPassword() != null) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado("ACTIVO");
        }
        return usuarioRepository.save(usuario);
    }

    // =====================================================
    // NUEVOS MÉTODOS PARA ADMINISTRADOR
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
            // ✅ Nombre: solo actualizar si no es null y no está vacío
            if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().trim().isEmpty()) {
                usuario.setNombre(usuarioActualizado.getNombre().trim());
            }

            // ✅ Email: solo actualizar si no es null y no está vacío
            if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().trim().isEmpty()) {
                usuario.setEmail(usuarioActualizado.getEmail().trim());
            }

            // ✅ Password: solo encriptar y actualizar si se proporciona uno nuevo (no vacío)
            if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().trim().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }

            // ✅ Estado: solo actualizar si no es null y no está vacío
            if (usuarioActualizado.getEstado() != null && !usuarioActualizado.getEstado().trim().isEmpty()) {
                usuario.setEstado(usuarioActualizado.getEstado().trim());
            }

            // ✅ Rol: solo actualizar si el rol tiene ID válido
            if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getId() != null) {
                rolRepository.findById(usuarioActualizado.getRol().getId())
                        .ifPresent(usuario::setRol);
            }



            // Guardar cambios
            return usuarioRepository.save(usuario);

        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public boolean eliminarLogico(Integer id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setEstado("INACTIVO"); // Soft delete
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
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
    public List<Usuario> buscarConFiltros(String nombre, String email, String estado, String rol) {
        // Si no hay filtros, retornar todos
        if (nombre == null && email == null && estado == null && rol == null) {
            return usuarioRepository.findAll();
        }

        // Filtrar en memoria (simple para empezar)
        return usuarioRepository.findAll().stream()
                .filter(u -> nombre == null || u.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(u -> email == null || u.getEmail().toLowerCase().contains(email.toLowerCase()))
                .filter(u -> estado == null || u.getEstado().equalsIgnoreCase(estado))
                .filter(u -> rol == null || (u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> stats = new HashMap<>();

        List<Usuario> todos = usuarioRepository.findAll();

        stats.put("totalUsuarios", todos.size());
        stats.put("usuariosActivos", todos.stream().filter(u -> "ACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("usuariosInactivos", todos.stream().filter(u -> "INACTIVO".equalsIgnoreCase(u.getEstado())).count());
        stats.put("totalAdmins", todos.stream().filter(u -> u.isAdmin()).count());
        stats.put("usuariosEstandar", todos.stream().filter(u -> u.isUsuarioEstandar()).count());

        return stats;
    }

    @Override
    public void actualizarUltimoAcceso(Integer usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            // Si tu entidad no tiene ultimoAcceso, puedes omitir esta línea
            // usuario.setUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeEmail(String email, Integer excluirId) {
        return usuarioRepository.findByEmail(email)
                .map(u -> !u.getId().equals(excluirId))
                .orElse(false);
    }
}