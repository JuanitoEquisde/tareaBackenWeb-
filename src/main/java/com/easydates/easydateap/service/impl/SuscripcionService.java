package com.easydates.easydateap.service.impl;

import com.easydates.easydateap.dto.SuscripcionDTO;
import com.easydates.easydateap.entity.Plan;
import com.easydates.easydateap.entity.Rol;
import com.easydates.easydateap.entity.Suscripcion;
import com.easydates.easydateap.entity.Usuario;
import com.easydates.easydateap.repository.PlanRepository;
import com.easydates.easydateap.repository.RolRepository;
import com.easydates.easydateap.repository.SuscripcionRepository;
import com.easydates.easydateap.repository.UsuarioRepository;
import com.easydates.easydateap.service.ISuscripcionService;
import com.easydates.easydateap.service.IUsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class SuscripcionService implements ISuscripcionService {

    @Autowired
    private SuscripcionRepository suscripcionRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private RolRepository rolRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Suscripcion> listarSuscripciones(Pageable pageable) {
        return suscripcionRepository.findAllWithDetails(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Plan> listarPlanesActivos() {
        return planRepository.findPlanesActivos();
    }

    @Override
    @Transactional
    public Suscripcion crearSuscripcion(SuscripcionDTO dto, Integer usuarioId) {
        if (usuarioId == null) {
            throw new RuntimeException("Usuario no autenticado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        if (dto.getPlanId() == null) {
            throw new RuntimeException("Plan no especificado");
        }

        Plan plan = planRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + dto.getPlanId()));

        String numeroTransaccion = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDate fechaInicio = LocalDate.now();
        LocalDate fechaFin = fechaInicio.plusDays(plan.getDuracionDias());

        Suscripcion.MetodoPago metodoPago = Suscripcion.MetodoPago.TARJETA;
        if (dto.getNumeroTarjeta() != null && !dto.getNumeroTarjeta().isEmpty()) {
            metodoPago = Suscripcion.MetodoPago.TARJETA;
        }

        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuario(usuario);
        suscripcion.setPlan(plan);
        suscripcion.setFechaInicio(fechaInicio);
        suscripcion.setFechaFin(fechaFin);
        suscripcion.setPrecioPagado(plan.getPrecio());
        suscripcion.setMetodoPago(metodoPago);
        suscripcion.setEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        suscripcion.setNumeroTransaccion(numeroTransaccion);

        Suscripcion suscripcionGuardada = suscripcionRepository.save(suscripcion);

        // Actualizar usuario a premium
        usuario.setEsPremium(true);
        usuario.setFechaPremiumExpiracion(fechaFin);
        usuarioRepository.save(usuario);

        return suscripcionGuardada;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Suscripcion> getSuscripcionActiva(Integer usuarioId) {
        return suscripcionRepository.findSuscripcionActivaByUsuarioId(usuarioId, Suscripcion.EstadoSuscripcion.ACTIVA);
    }

    @Override
    @Transactional
    public void verificarYActualizarSuscripcionesVencidas() {
        List<Suscripcion> vencidas = suscripcionRepository.findSuscripcionesVencidas(
                LocalDate.now(), Suscripcion.EstadoSuscripcion.ACTIVA);

        for (Suscripcion s : vencidas) {
            s.setEstado(Suscripcion.EstadoSuscripcion.VENCIDA);
            suscripcionRepository.save(s);

            Usuario usuario = s.getUsuario();
            usuario.setEsPremium(false);
            usuario.setFechaPremiumExpiracion(null);
            usuarioRepository.save(usuario);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalSuscripcionesActivas() {
        return suscripcionRepository.countSuscripcionesActivas(Suscripcion.EstadoSuscripcion.ACTIVA);
    }

    @Override
    public Page<Suscripcion> buscarConFiltrosAdmin(
            String estado,
            String plan,
            String usuario,
            String fechaInicio,
            String fechaFin,
            Pageable pageable) {

        // ✅ Convertir String a Enum para el estado (MANEJO SEGURO DE NULL)
        Suscripcion.EstadoSuscripcion estadoEnum = null;
        if (estado != null && !estado.trim().isEmpty()) {
            try {
                estadoEnum = Suscripcion.EstadoSuscripcion.valueOf(estado.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Si no es un estado válido, lo dejamos null
                estadoEnum = null;
            }
        }

        // ✅ Convertir fechas String a LocalDate (MANEJO SEGURO DE NULL)
        LocalDate fechaInicioDate = null;
        LocalDate fechaFinDate = null;

        if (fechaInicio != null && !fechaInicio.trim().isEmpty()) {
            try {
                fechaInicioDate = LocalDate.parse(fechaInicio);
            } catch (Exception e) {
                // Si no se puede parsear, lo dejamos null
                fechaInicioDate = null;
            }
        }

        if (fechaFin != null && !fechaFin.trim().isEmpty()) {
            try {
                fechaFinDate = LocalDate.parse(fechaFin);
            } catch (Exception e) {
                // Si no se puede parsear, lo dejamos null
                fechaFinDate = null;
            }
        }

        // ✅ Llamar al repository con los parámetros convertidos
        return suscripcionRepository.buscarConFiltrosAdmin(
                estadoEnum,
                plan,
                usuario,
                fechaInicioDate,
                fechaFinDate,
                pageable);
    }

    @Override
    public Map<String, Long> obtenerEstadisticasSuscripciones() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("total", suscripcionRepository.count());
        stats.put("activas", suscripcionRepository.countByEstado(Suscripcion.EstadoSuscripcion.ACTIVA));
        stats.put("canceladas", suscripcionRepository.countByEstado(Suscripcion.EstadoSuscripcion.CANCELADA));
        stats.put("vencidas", suscripcionRepository.countByEstado(Suscripcion.EstadoSuscripcion.VENCIDA));

        BigDecimal ingresos = suscripcionRepository.sumarPreciosPagadosByEstado(Suscripcion.EstadoSuscripcion.ACTIVA);
        stats.put("ingresosTotales", ingresos != null ? ingresos.longValue() : 0L);

        return stats;
    }

    @Override
    @Transactional
    public boolean cambiarEstadoSuscripcion(Integer id, String nuevoEstado) {
        return suscripcionRepository.findById(id).map(suscripcion -> {
            try {
                Suscripcion.EstadoSuscripcion estadoEnum = Suscripcion.EstadoSuscripcion.valueOf(nuevoEstado);

                suscripcion.setEstado(estadoEnum);
                suscripcion.setFechaActualizacion(LocalDateTime.now());

                if (estadoEnum == Suscripcion.EstadoSuscripcion.CANCELADA ||
                        estadoEnum == Suscripcion.EstadoSuscripcion.VENCIDA) {

                    Usuario usuario = suscripcion.getUsuario();
                    long activas = suscripcionRepository.countByUsuarioIdAndEstado(
                            usuario.getId(),
                            Suscripcion.EstadoSuscripcion.ACTIVA
                    );

                    if (activas == 0) {
                        usuario.setEsPremium(false);
                        usuario.setFechaPremiumExpiracion(null);
                        if (usuario.getRol() != null && "PREMIUM".equals(usuario.getRol().getNombre())) {
                            Rol rolUsuario = rolRepository.findByNombre("USUARIO")
                                    .orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado"));
                            usuario.setRol(rolUsuario);
                        }
                        usuarioRepository.save(usuario);
                    }
                }
                else if (estadoEnum == Suscripcion.EstadoSuscripcion.ACTIVA) {
                    Usuario usuario = suscripcion.getUsuario();
                    usuario.setEsPremium(true);
                    usuario.setFechaPremiumExpiracion(suscripcion.getFechaFin());
                    usuarioRepository.save(usuario);
                }

                suscripcionRepository.save(suscripcion);
                return true;
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado no válido: " + nuevoEstado);
            }
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean eliminarSuscripcion(Integer id) {
        return suscripcionRepository.findById(id).map(suscripcion -> {
            suscripcion.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);
            suscripcion.setFechaActualizacion(LocalDateTime.now());

            Usuario usuario = suscripcion.getUsuario();
            long activas = suscripcionRepository.countByUsuarioIdAndEstado(
                    usuario.getId(),
                    Suscripcion.EstadoSuscripcion.ACTIVA
            );
            if (activas == 0) {
                usuarioService.actualizarRolPremium(usuario.getId(), null, false, null);
            }

            suscripcionRepository.save(suscripcion);
            return true;
        }).orElse(false);
    }

    @Override
    public Map<String, Object> obtenerDetalleSuscripcion(Integer id) {
        Map<String, Object> detalle = new HashMap<>();

        suscripcionRepository.findById(id).ifPresent(suscripcion -> {
            detalle.put("id", suscripcion.getId());
            detalle.put("usuario", suscripcion.getUsuario().getNombre());
            detalle.put("email", suscripcion.getUsuario().getEmail());
            detalle.put("plan", suscripcion.getPlan().getNombre());
            detalle.put("precio", suscripcion.getPrecioPagado());
            detalle.put("fechaInicio", suscripcion.getFechaInicio());
            detalle.put("fechaFin", suscripcion.getFechaFin());
            detalle.put("estado", suscripcion.getEstado());
            detalle.put("metodoPago", suscripcion.getMetodoPago() != null ? suscripcion.getMetodoPago().name() : null);
            detalle.put("transaccion", suscripcion.getNumeroTransaccion());
            detalle.put("fechaCreacion", suscripcion.getFechaCreacion());
        });

        return detalle;
    }

    @Override
    @Transactional
    public boolean editarSuscripcion(Integer id, Map<String, Object> datosActualizados) {
        return suscripcionRepository.findById(id).map(suscripcion -> {
            try {
                if (datosActualizados.get("estado") != null) {
                    String nuevoEstado = (String) datosActualizados.get("estado");
                    suscripcion.setEstado(Suscripcion.EstadoSuscripcion.valueOf(nuevoEstado));
                }
                if (datosActualizados.get("fechaInicio") != null) {
                    String fechaInicioStr = (String) datosActualizados.get("fechaInicio");
                    suscripcion.setFechaInicio(LocalDate.parse(fechaInicioStr));
                }
                if (datosActualizados.get("fechaFin") != null) {
                    String fechaFinStr = (String) datosActualizados.get("fechaFin");
                    suscripcion.setFechaFin(LocalDate.parse(fechaFinStr));
                }
                if (datosActualizados.get("precioPagado") != null) {
                    Number precio = (Number) datosActualizados.get("precioPagado");
                    suscripcion.setPrecioPagado(new BigDecimal(precio.doubleValue()));
                }

                suscripcion.setFechaActualizacion(LocalDateTime.now());
                suscripcionRepository.save(suscripcion);
                return true;
            } catch (Exception e) {
                throw new RuntimeException("Error al actualizar suscripción: " + e.getMessage());
            }
        }).orElse(false);
    }

    @Override
    @Transactional
    public void actualizarEstadoPremiumUsuario(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        long suscripcionesActivas = suscripcionRepository.countByUsuarioIdAndEstado(
                usuarioId,
                Suscripcion.EstadoSuscripcion.ACTIVA
        );

        boolean esPremium = (suscripcionesActivas > 0);
        usuario.setEsPremium(esPremium);

        if (!esPremium) {
            usuario.setFechaPremiumExpiracion(null);
        }

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticasReporte() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalSuscripciones", suscripcionRepository.count());
        stats.put("suscripcionesActivas", suscripcionRepository.countByEstado(Suscripcion.EstadoSuscripcion.ACTIVA));
        stats.put("suscripcionesVencidas", suscripcionRepository.countByEstado(Suscripcion.EstadoSuscripcion.VENCIDA));
        stats.put("ingresosTotales", suscripcionRepository.sumarPreciosPagados());
        stats.put("suscripcionesPorPlan", suscripcionRepository.contarPorPlan());

        return stats;
    }

    @Override
    @Transactional
    public void cancelarSuscripcionesPorUsuario(Integer usuarioId) {
        // Buscar suscripciones activas del usuario
        List<Suscripcion> suscripcionesActivas = suscripcionRepository
                .findByUsuarioIdAndEstado(usuarioId, Suscripcion.EstadoSuscripcion.ACTIVA);

        // Cancelar cada suscripción
        for (Suscripcion s : suscripcionesActivas) {
            s.setEstado(Suscripcion.EstadoSuscripcion.CANCELADA);
            s.setFechaActualizacion(LocalDateTime.now());
            suscripcionRepository.save(s);
        }

        System.out.println("✅ Suscripciones canceladas para usuario ID: " + usuarioId +
                " (Total: " + suscripcionesActivas.size() + ")");
    }

}