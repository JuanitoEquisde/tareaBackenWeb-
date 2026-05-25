package com.easydates.easydateap.security;

import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Tu BD usa email como identificador
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        // Convertir tu entidad Usuario a UserDetails de Spring Security
        return User.builder()
                .username(usuario.getEmail())  // Spring Security espera "username", pero usamos email
                .password(usuario.getPassword())  // Debe estar encriptada con BCrypt
                .roles(usuario.getRol().getNombre())  // Roles para autorización
                .disabled(!"ACTIVO".equalsIgnoreCase(usuario.getEstado()))  // Desactivar si no está activo
                .build();
    }
}