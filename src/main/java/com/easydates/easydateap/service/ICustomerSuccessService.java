package com.easydates.easydateap.service;

import com.easydates.easydateap.dto.CustomerSuccessDTO;

import java.util.List;
import java.util.Map;

public interface ICustomerSuccessService {

    List<CustomerSuccessDTO> obtenerTodosLosUsuariosConHealthScore();

    CustomerSuccessDTO obtenerDetalleUsuario(Integer usuarioId);

    Map<String, Object> obtenerMetricasGenerales();

    List<CustomerSuccessDTO> obtenerUsuariosEnRiesgo(String nivelRiesgo);

    List<CustomerSuccessDTO> obtenerOportunidadesUpsell();
}