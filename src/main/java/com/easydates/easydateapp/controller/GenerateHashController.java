package com.easydates.easydateapp.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenerateHashController {

    @GetMapping("/generate-hash")
    public String generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("✅ Hash BCrypt para '" + password + "':");
        System.out.println(hash);
        System.out.println("Longitud: " + hash.length());

        return "<h1>Hash BCrypt generado:</h1>" + "<p><strong>Password:</strong> " + password + "</p>" + "<p><strong>Hash:</strong> " + hash + "</p>" + "<p><strong>Longitud:</strong> " + hash.length() + "</p>" + "<p><i>Copia este hash y ejecuta el UPDATE en SQL Server</i></p>";
    }
}