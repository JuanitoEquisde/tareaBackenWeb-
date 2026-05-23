package com.easydates.easydateapp.service.impl;

import com.easydates.easydateapp.dto.TareaDTO;
import com.easydates.easydateapp.entity.*;
import com.easydates.easydateapp.repository.*;
import com.easydates.easydateapp.service.ITareaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class TareaServiceImpl implements ITareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private EtiquetaRepository etiquetaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =====================================================
    // 🔹 MÉTODOS EXISTENTES (NO MODIFICAR - Ya funcionan)
    // =====================================================

    @Override
    public Tarea crearTarea(TareaDTO dto, Integer usuarioId) {
        Tarea tarea = new Tarea();
        mapearDTOaEntidad(dto, tarea, usuarioId);
        return tareaRepository.save(tarea);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tarea> obtenerTarea(Integer id) {
        return tareaRepository.findById(id)
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarTareasPorUsuario(Integer usuarioId) {
        return tareaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarPorEstado(Integer usuarioId, String estadoTarea) {
        return tareaRepository.findByUsuarioIdAndEstadoAndEstadoTarea(
                usuarioId, "ACTIVO", estadoTarea.toUpperCase());
    }

    @Override
    public Tarea actualizarTarea(Integer id, TareaDTO dto) {
        Tarea tarea = obtenerTarea(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada"));

        mapearDTOaEntidad(dto, tarea, tarea.getUsuario().getId());
        return tareaRepository.save(tarea);
    }

    @Override
    public boolean eliminarTarea(Integer id) {
        return obtenerTarea(id).map(tarea -> {
            tareaRepository.delete(tarea);
            return true;
        }).orElse(false);
    }

    @Override
    public boolean eliminarLogico(Integer id) {
        return obtenerTarea(id).map(tarea -> {
            tarea.setEstado("ELIMINADO");
            tareaRepository.save(tarea);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional
    public boolean cambiarEstadoTarea(Integer id, String nuevoEstado) {
        Optional<Tarea> tareaOpt = tareaRepository.findById(id);

        if (tareaOpt.isPresent() && tareaOpt.get().getEstado().equals("ACTIVO")) {
            Tarea tarea = tareaOpt.get();
            tarea.setEstadoTarea(nuevoEstado);
            tareaRepository.save(tarea);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> buscarPorTermino(Integer usuarioId, String termino) {
        return tareaRepository.buscarPorTermino(usuarioId, termino);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarPorCategoria(Integer usuarioId, Integer categoriaId) {
        if (categoriaId == null) {
            return listarTareasPorUsuario(usuarioId);
        }
        return tareaRepository.findByUsuarioIdAndCategoriaId(usuarioId, categoriaId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTotal(Integer usuarioId) {
        return tareaRepository.contarTotalPorUsuario(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes(Integer usuarioId) {
        return tareaRepository.contarPendientesPorUsuario(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarCompletadas(Integer usuarioId) {
        return tareaRepository.contarCompletadasPorUsuario(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarUrgentes(Integer usuarioId) {
        return tareaRepository.contarUrgentesPorUsuario(usuarioId);
    }

    // ✅ Métodos para el calendario
    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarPorFecha(Integer usuarioId, LocalDate fecha) {
        return tareaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVO")
                .stream()
                .filter(t -> t.getFechaLimite() != null && t.getFechaLimite().isEqual(fecha))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarPorRangoFechas(Integer usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        return tareaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVO")
                .stream()
                .filter(t -> t.getFechaLimite() != null)
                .filter(t -> !t.getFechaLimite().isBefore(fechaInicio) && !t.getFechaLimite().isAfter(fechaFin))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarConFiltros(Integer usuarioId, String estado, String prioridad, Integer categoriaId, String busqueda, String sortBy, String sortDir) {

        System.out.println("🔍 [SERVICE] listarConFiltros - usuarioId: " + usuarioId);

        List<Tarea> tareas = tareaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVO");
        System.out.println("🔍 [SERVICE] Tareas base encontradas: " + tareas.size());

        if (estado != null && !estado.isEmpty() && !estado.equals("TODOS")) {
            System.out.println("🔍 [SERVICE] Filtrando por estado: " + estado);
            tareas = tareas.stream()
                    .filter(t -> t.getEstadoTarea() != null && t.getEstadoTarea().equalsIgnoreCase(estado))
                    .collect(Collectors.toList());
        }

        if (prioridad != null && !prioridad.isEmpty() && !prioridad.equals("TODOS")) {
            System.out.println("🔍 [SERVICE] Filtrando por prioridad: " + prioridad);
            tareas = tareas.stream()
                    .filter(t -> t.getPrioridad() != null && t.getPrioridad().equalsIgnoreCase(prioridad))
                    .collect(Collectors.toList());
        }

        if (categoriaId != null) {
            System.out.println("🔍 [SERVICE] Filtrando por categoría ID: " + categoriaId);
            tareas = tareas.stream()
                    .filter(t -> t.getCategoria() != null && t.getCategoria().getId().equals(categoriaId))
                    .collect(Collectors.toList());
        }

        if (busqueda != null && !busqueda.isEmpty()) {
            System.out.println("🔍 [SERVICE] Filtrando por búsqueda: " + busqueda);
            String busquedaLower = busqueda.toLowerCase();
            tareas = tareas.stream()
                    .filter(t ->
                            (t.getTitulo() != null && t.getTitulo().toLowerCase().contains(busquedaLower)) ||
                                    (t.getDescripcion() != null && t.getDescripcion().toLowerCase().contains(busquedaLower))
                    )
                    .collect(Collectors.toList());
        }

        System.out.println("🔍 [SERVICE] Tareas después de filtros: " + tareas.size());

        Comparator<Tarea> comparator = Comparator.comparing(t -> t.getFechaLimite(), Comparator.nullsLast(Comparator.naturalOrder()));

        if ("titulo".equals(sortBy)) {
            comparator = Comparator.comparing(Tarea::getTitulo, Comparator.nullsLast(Comparator.naturalOrder()));
        } else if ("prioridad".equals(sortBy)) {
            comparator = Comparator.comparing(Tarea::getPrioridad, Comparator.nullsLast(Comparator.naturalOrder()));
        }

        if ("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }

        List<Tarea> resultado = tareas.stream().sorted(comparator).collect(Collectors.toList());
        System.out.println("✅ [SERVICE] Tareas finales a retornar: " + resultado.size());

        return resultado;
    }

    // =====================================================
    // 🔹 NUEVOS MÉTODOS PARA ADMINISTRADOR
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> listarTodasLasTareas() {
        System.out.println("🔍 [ADMIN] Listando TODAS las tareas del sistema");
        return tareaRepository.findByEstado("ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tarea> buscarTareasAdmin(String titulo, String prioridad, String estado, String nombreUsuario) {
        System.out.println("🔍 [ADMIN] Buscar tareas - titulo: " + titulo +
                ", prioridad: " + prioridad +
                ", estado: " + estado +
                ", usuario: " + nombreUsuario);

        // Usar la query optimizada del repository
        List<Tarea> tareas = tareaRepository.buscarTareasAdmin(
                titulo != null && !titulo.trim().isEmpty() ? titulo : null,
                prioridad != null && !prioridad.trim().isEmpty() ? prioridad.toUpperCase() : null,
                estado != null && !estado.trim().isEmpty() ? estado.toUpperCase() : null,
                nombreUsuario != null && !nombreUsuario.trim().isEmpty() ? nombreUsuario : null
        );

        System.out.println("✅ [ADMIN] Tareas encontradas: " + tareas.size());
        return tareas;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTareasTotales() {
        return tareaRepository.countByEstado("ACTIVO");
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTareasPorEstado(String estado) {
        if (estado == null || estado.isEmpty()) {
            return contarTareasTotales();
        }
        return tareaRepository.countByEstadoAndEstadoTarea("ACTIVO", estado.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public long contarTareasPorPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isEmpty()) {
            return contarTareasTotales();
        }
        return tareaRepository.countByEstadoAndPrioridad("ACTIVO", prioridad.toUpperCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tarea> obtenerTareaConDetalles(Integer id) {
        return tareaRepository.findById(id)
                .filter(t -> "ACTIVO".equalsIgnoreCase(t.getEstado()))
                .map(tarea -> {
                    if (tarea.getEtiquetas() != null) {
                        tarea.getEtiquetas().size();
                    }
                    return tarea;
                });
    }

    @Override
    public boolean eliminarPermanente(Integer id) {
        return tareaRepository.findById(id).map(tarea -> {
            if (tarea.getEtiquetas() != null) {
                tarea.getEtiquetas().clear();
            }
            tareaRepository.delete(tarea);
            System.out.println("🗑️ [ADMIN] Tarea eliminada permanentemente: " + id);
            return true;
        }).orElse(false);
    }

    // =====================================================
    // 🔹 MÉTODO HELPER (NO MODIFICAR)
    // =====================================================

    private void mapearDTOaEntidad(TareaDTO dto, Tarea tarea, Integer usuarioId) {
        tarea.setTitulo(dto.getTitulo());
        tarea.setDescripcion(dto.getDescripcion());
        tarea.setPrioridad(dto.getPrioridad() != null ? dto.getPrioridad().toUpperCase() : "MEDIA");
        tarea.setEstadoTarea(dto.getEstadoTarea() != null ? dto.getEstadoTarea().toUpperCase() : "PENDIENTE");
        tarea.setFechaLimite(dto.getFechaLimite());
        tarea.setEstado("ACTIVO");

        usuarioRepository.findById(usuarioId).ifPresent(tarea::setUsuario);

        if (dto.getCategoriaId() != null) {
            categoriaRepository.findById(dto.getCategoriaId())
                    .ifPresent(tarea::setCategoria);
        }

        if (dto.getEtiquetasIds() != null && !dto.getEtiquetasIds().isEmpty()) {
            Set<Etiqueta> etiquetas = dto.getEtiquetasIds().stream()
                    .map(etiquetaRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            tarea.setEtiquetas(etiquetas);
        }
    }
}