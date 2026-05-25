package com.easydates.easydateap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioPasswordDTO {
    private String passwordActual;
    private String passwordNuevo;
    private String passwordConfirmacion;
}