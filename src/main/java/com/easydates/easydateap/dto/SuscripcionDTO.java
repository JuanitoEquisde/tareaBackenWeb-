package com.easydates.easydateap.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SuscripcionDTO {
    private Integer planId;
    private String numeroTarjeta;
    private String nombreTitular;
    private String fechaExpiracion;
    private String cvv;
    private BigDecimal monto;

    // ✅ NUEVO: Método de pago seleccionado por el usuario
    private String metodoPago;  // "TARJETA", "PAYPAL", "TRANSFERENCIA"
}