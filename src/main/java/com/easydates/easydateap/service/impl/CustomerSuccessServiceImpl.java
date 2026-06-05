package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.dto.CustomerSuccessDTO;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.ICustomerSuccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomerSuccessServiceImpl implements ICustomerSuccessService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<CustomerSuccessDTO> obtenerTodosLosUsuariosConHealthScore() {
        List<CustomerSuccessDTO> usuarios = usuarioRepository.findAllCustomerSuccessData();

        // Calcular días sin actividad para cada usuario
        for (CustomerSuccessDTO usuario : usuarios) {
            if (usuario.getUltimaActividad() != null) {
                int diasSinActividad = (int) ChronoUnit.DAYS.between(
                        usuario.getUltimaActividad(),
                        LocalDate.now()
                );
                usuario.setDiasSinActividad(diasSinActividad);

                // Recalcular health score con la información completa
                // (El DTO ya tiene el cálculo pero necesitamos actualizarlo)
                recalcularHealthScore(usuario);
            } else {
                usuario.setDiasSinActividad(999); // Sin actividad nunca
                recalcularHealthScore(usuario);
            }
        }

        return usuarios;
    }

    @Override
    public CustomerSuccessDTO obtenerDetalleUsuario(Integer usuarioId) {
        return obtenerTodosLosUsuariosConHealthScore().stream()
                .filter(u -> u.getUsuarioId().equals(usuarioId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Map<String, Object> obtenerMetricasGenerales() {
        List<CustomerSuccessDTO> todos = obtenerTodosLosUsuariosConHealthScore();

        Map<String, Object> metricas = new HashMap<>();

        // Total usuarios
        metricas.put("totalUsuarios", todos.size());

        // Usuarios por nivel de riesgo
        long bajo = todos.stream().filter(u -> "BAJO".equals(u.getNivelRiesgo())).count();
        long medio = todos.stream().filter(u -> "MEDIO".equals(u.getNivelRiesgo())).count();
        long alto = todos.stream().filter(u -> "ALTO".equals(u.getNivelRiesgo())).count();
        long critico = todos.stream().filter(u -> "CRITICO".equals(u.getNivelRiesgo())).count();

        metricas.put("riesgoBajo", bajo);
        metricas.put("riesgoMedio", medio);
        metricas.put("riesgoAlto", alto);
        metricas.put("riesgoCritico", critico);

        // Health score promedio
        double avgScore = todos.stream()
                .mapToInt(CustomerSuccessDTO::getHealthScore)
                .average()
                .orElse(0.0);
        metricas.put("healthScorePromedio", Math.round(avgScore * 100.0) / 100.0);

        // Tasa de retención (usuarios con health score >= 60)
        long retenidos = todos.stream()
                .filter(u -> u.getHealthScore() >= 60)
                .count();
        double tasaRetencion = todos.size() > 0 ? (double) retenidos / todos.size() * 100 : 0;
        metricas.put("tasaRetencion", Math.round(tasaRetencion * 100.0) / 100.0);

        // Usuarios premium vs gratuitos
        long premium = todos.stream().filter(CustomerSuccessDTO::getEsPremium).count();
        metricas.put("usuariosPremium", premium);
        metricas.put("usuariosGratuitos", todos.size() - premium);

        return metricas;
    }

    @Override
    public List<CustomerSuccessDTO> obtenerUsuariosEnRiesgo(String nivelRiesgo) {
        return obtenerTodosLosUsuariosConHealthScore().stream()
                .filter(u -> nivelRiesgo.equals(u.getNivelRiesgo()))
                .sorted((u1, u2) -> u1.getHealthScore().compareTo(u2.getHealthScore()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerSuccessDTO> obtenerOportunidadesUpsell() {
        return obtenerTodosLosUsuariosConHealthScore().stream()
                .filter(u -> !u.getEsPremium() && u.getHealthScore() >= 70)
                .sorted((u1, u2) -> u2.getHealthScore().compareTo(u1.getHealthScore()))
                .collect(Collectors.toList());
    }

    private void recalcularHealthScore(CustomerSuccessDTO dto) {
        int score = 100;

        // Penalizar por tareas vencidas
        if (dto.getTareasVencidas() > 0) {
            score -= Math.min(dto.getTareasVencidas() * 10, 30);
        }

        // Penalizar por inactividad
        if (dto.getDiasSinActividad() > 7) {
            score -= Math.min((dto.getDiasSinActividad() - 7) * 2, 25);
        }

        // Penalizar si la suscripción está por vencer
        if (dto.getFechaVencimiento() != null) {
            long diasParaVencer = LocalDate.now().until(dto.getFechaVencimiento()).getDays();
            if (diasParaVencer < 0) {
                score -= 30;
            } else if (diasParaVencer < 7) {
                score -= 15;
            }
        }

        // Bonus por completitud
        if (dto.getTareasTotales() > 0) {
            double tasaCompletitud = (double) dto.getTareasCompletadas() / dto.getTareasTotales();
            if (tasaCompletitud > 0.8) {
                score += 10;
            }
        }

        dto.setHealthScore(Math.max(0, Math.min(100, score)));

        // Determinar nivel de riesgo
        if (dto.getHealthScore() >= 80) {
            dto.setNivelRiesgo("BAJO");
            dto.setRecomendacion("Usuario saludable - Mantener engagement");
        } else if (dto.getHealthScore() >= 60) {
            dto.setNivelRiesgo("MEDIO");
            dto.setRecomendacion("Monitorear actividad - Enviar recordatorios");
        } else if (dto.getHealthScore() >= 40) {
            dto.setNivelRiesgo("ALTO");
            dto.setRecomendacion("Riesgo de churn - Contactar proactivamente");
        } else {
            dto.setNivelRiesgo("CRITICO");
            dto.setRecomendacion("Churn inminente - Intervención inmediata requerida");
        }
    }
}