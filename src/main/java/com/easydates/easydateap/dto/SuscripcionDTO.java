package com.easydates.easydateap.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SuscripcionDTO {
    private Integer planId;
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaExpiracion; // MM/YY
    private String cvv;
    private BigDecimal monto;
}