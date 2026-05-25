package com.easydates.easydateap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilDTO {
    private String nombre;
    private String email;
    private String tema; // claro, oscuro, elegante
}