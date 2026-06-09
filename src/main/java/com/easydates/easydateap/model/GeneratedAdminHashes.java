package com.easydates.easydateap.model;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratedAdminHashes {
    public static void main(String[] args) {
        // Usar EXACTAMENTE el mismo encoder que tu SecurityConfig
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

        String[] passwords = {"1", "2", "3", "4"};

        System.out.println("=== HASHES BCrypt REALES PARA ADMIN ===\n");
        System.out.println("// Copia estos hashes en tu SQL:\n");

        for (String pwd : passwords) {
            String hash = encoder.encode(pwd);
            System.out.println("-- Admin con contraseña \"" + pwd + "\"");
            System.out.println("INSERT INTO usuarios (nombre, email, passwordd, rol_id, estado)");
            System.out.println("VALUES ('Admin " + numeroATexto(Integer.parseInt(pwd)) + "', 'admin" + pwd + "@notygo.com', '" + hash + "', 1, 'ACTIVO');");
            System.out.println();
        }
    }

    private static String numeroATexto(int n) {
        String[] textos = {"", "Uno", "Dos", "Tres", "Cuatro"};
        return textos[n];
    }
}