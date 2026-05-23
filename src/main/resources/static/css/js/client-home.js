/**
 * NOTYGO - CLIENT HOME JAVASCRIPT
 * Funcionalidades principales + Drag & Drop
 */

document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ NotyGo Dashboard cargado');
    initSortable();
    initViewToggle();
    initSearch();
    initDatePickers();
});

// ==========================================
// DRAG AND DROP - SORTABLEJS
// ==========================================
function initSortable() {
    const columns = document.querySelectorAll('.kanban-column-body');
    columns.forEach(column => {
        new Sortable(column, {
            group: 'kanban',
            animation: 150,
            ghostClass: 'dragging',
            dragClass: 'dragging',
            delay: 100,
            delayOnTouchOnly: true,
            touchStartThreshold: 5,
            onEnd: function(evt) {
                const taskId = evt.item.getAttribute('data-id');
                const newStatus = evt.to.getAttribute('data-estado');
                if (evt.from !== evt.to && taskId) {
                    actualizarEstadoTarea(taskId, newStatus);
                }
            }
        });
    });
}

// ==========================================
// CAMBIAR VISTA KANBAN/LISTA
// ==========================================
function initViewToggle() {
    const btnKanban = document.getElementById('btnVistaKanban');
    const btnLista = document.getElementById('btnVistaLista');
    const kanbanBoard = document.getElementById('kanbanBoard');
    const listaView = document.getElementById('listaView');
    if (!btnKanban || !btnLista) return;
    btnKanban.addEventListener('click', function() {
        kanbanBoard?.classList.remove('d-none');
        listaView?.classList.add('d-none');
        btnKanban.classList.add('active');
        btnLista.classList.remove('active');
        localStorage.setItem('notygo-view', 'kanban');
    });
    btnLista.addEventListener('click', function() {
        kanbanBoard?.classList.add('d-none');
        listaView?.classList.remove('d-none');
        btnLista.classList.add('active');
        btnKanban.classList.remove('active');
        localStorage.setItem('notygo-view', 'lista');
    });
    const savedView = localStorage.getItem('notygo-view');
    if (savedView === 'lista' && btnLista) {
        btnLista.click();
    }
}

// ==========================================
// BÚSQUEDA EN TIEMPO REAL
// ==========================================
function initSearch() {
    const searchInput = document.querySelector('.search-form input');
    if (!searchInput) return;
    let timeout;
    searchInput.addEventListener('input', function(e) {
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            buscarTareas(e.target.value);
        }, 300);
    });
}

function buscarTareas(termino) {
    const cards = document.querySelectorAll('.task-card');
    const term = termino.toLowerCase().trim();
    if (!term) {
        cards.forEach(card => card.style.display = '');
        return;
    }
    cards.forEach(card => {
        const titulo = card.querySelector('.task-title')?.textContent.toLowerCase() || '';
        const descripcion = card.querySelector('.task-description')?.textContent.toLowerCase() || '';
        const categoria = card.querySelector('.task-category')?.textContent.toLowerCase() || '';
        const coincide = titulo.includes(term) || descripcion.includes(term) || categoria.includes(term);
        card.style.display = coincide ? '' : 'none';
    });
}

// ==========================================
// DATE PICKERS
// ==========================================
function initDatePickers() {
    const fechaInput = document.getElementById('fechaLimite');
    if (fechaInput) {
        const hoy = new Date().toISOString().split('T')[0];
        fechaInput.setAttribute('min', hoy);
    }
}

// ==========================================
// ACTUALIZAR ESTADO DE TAREA (AJAX)
// ==========================================
function actualizarEstadoTarea(taskId, nuevoEstado) {
    if (!taskId) return;
    const taskCard = document.querySelector(`[data-id="${taskId}"]`);
    if (taskCard) taskCard.style.opacity = '0.6';
    fetch(`/cliente/tareas/${taskId}/estado`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: `estado=${encodeURIComponent(nuevoEstado)}`
    })
    .then(response => {
        if (response.ok) {
            mostrarNotificacion('✅ Estado actualizado', 'success');
            actualizarContadores();
        } else {
            throw new Error('Error en la respuesta');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        mostrarNotificacion('❌ Error al actualizar', 'error');
        location.reload();
    })
    .finally(() => {
        if (taskCard) taskCard.style.opacity = '1';
    });
}

// ==========================================
// ACTUALIZAR CONTADORES DE COLUMNAS
// ==========================================
function actualizarContadores() {
    document.querySelectorAll('.kanban-column').forEach(column => {
        const count = column.querySelectorAll('.task-card').length;
        const counter = column.querySelector('.task-counter');
        if (counter) counter.textContent = count;
    });
}

// ==========================================
// NOTIFICACIONES TOAST
// ==========================================
function mostrarNotificacion(mensaje, tipo = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const toast = document.createElement('div');
    const clases = {
        success: 'bg-success',
        error: 'bg-danger',
        warning: 'bg-warning text-dark',
        info: 'bg-info text-dark'
    };
    toast.className = `toast align-items-center text-white ${clases[tipo] || clases.info} border-0`;
    toast.setAttribute('role', 'alert');
    toast.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">${mensaje}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    `;
    container.appendChild(toast);
    const bsToast = new bootstrap.Toast(toast, { delay: 3000 });
    bsToast.show();
    toast.addEventListener('hidden.bs.toast', () => toast.remove());
}

// ==========================================
// FUNCIONES GLOBALES
// ==========================================
window.editarTarea = function(id) {
    console.log('Editar tarea:', id);
    mostrarNotificacion('🔧 Función en desarrollo', 'info');
};

window.eliminarTarea = function(id) {
    if (confirm('¿Estás seguro de eliminar esta tarea?')) {
        fetch(`/cliente/tareas/${id}/eliminar`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
        })
        .then(response => {
            if (response.ok) {
                document.querySelector(`[data-id="${id}"]`)?.remove();
                mostrarNotificacion('🗑️ Tarea eliminada', 'success');
                actualizarContadores();
            }
        })
        .catch(() => mostrarNotificacion('❌ Error al eliminar', 'error'));
    }
};
/* ==========================================
   DRAG & DROP Y MODALES
   ========================================== */

let currentDragTask = null;

// Inicializar drag and drop
function initDragAndDrop() {
    const taskCards = document.querySelectorAll('.task-card[draggable="true"]');

    taskCards.forEach(card => {
        card.addEventListener('dragstart', handleDragStart);
        card.addEventListener('dragend', handleDragEnd);
    });

    // Drop zones (columnas)
    const columns = document.querySelectorAll('.kanban-column-body');
    columns.forEach(column => {
        column.addEventListener('dragover', handleDragOver);
        column.addEventListener('dragleave', handleDragLeave);
        column.addEventListener('drop', handleDrop);
    });
}

function handleDragStart(e) {
    currentDragTask = this;
    this.classList.add('dragging');
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', this.dataset.id);

    // Mostrar progress bar
    showProgressBar(this.dataset.estadoTarea);
}

function handleDragEnd(e) {
    this.classList.remove('dragging');
    hideProgressBar();
    currentDragTask = null;
}

function handleDragOver(e) {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    this.classList.add('drag-over');
}

function handleDragLeave(e) {
    this.classList.remove('drag-over');
}

function handleDrop(e) {
    e.preventDefault();
    this.classList.remove('drag-over');

    const taskId = e.dataTransfer.getData('text/plain');
    const newEstado = this.dataset.estado;

    if (taskId && newEstado) {
        actualizarEstadoTarea(taskId, newEstado);
    }
}

// Mostrar progress bar con estado actual
function showProgressBar(estadoActual) {
    const progressBar = document.getElementById('statusProgressBar');
    const progressFill = document.getElementById('progressFill');
    const steps = document.querySelectorAll('.step');

    progressBar.classList.add('show');

    // Calcular progreso
    let progreso = 0;
    if (estadoActual === 'PENDIENTE') progreso = 0;
    else if (estadoActual === 'EN_PROGRESO') progreso = 50;
    else if (estadoActual === 'TERMINADO') progreso = 100;

    progressFill.style.width = progreso + '%';

    // Actualizar steps
    steps.forEach((step, index) => {
        step.classList.remove('active', 'completed');
        const stepEstado = step.dataset.estado;

        if (stepEstado === estadoActual) {
            step.classList.add('active');
        } else if (
            (estadoActual === 'EN_PROGRESO' && stepEstado === 'PENDIENTE') ||
            (estadoActual === 'TERMINADO' && stepEstado !== 'TERMINADO')
        ) {
            step.classList.add('completed');
        }
    });

    // Click en steps para cambiar estado
    steps.forEach(step => {
        step.onclick = () => {
            if (currentDragTask) {
                const nuevoEstado = step.dataset.estado;
                actualizarEstadoTarea(currentDragTask.dataset.id, nuevoEstado);
            }
        };
    });
}

function hideProgressBar() {
    const progressBar = document.getElementById('statusProgressBar');
    progressBar.classList.remove('show');
}

// Actualizar estado vía AJAX
function actualizarEstadoTarea(taskId, nuevoEstado) {
    fetch(`/cliente/tareas/${taskId}/estado`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ estado: nuevoEstado })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            // Actualizar UI
            location.reload();
        } else {
            alert('Error al actualizar el estado');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al actualizar el estado');
    });
}

// ==========================================
// MODAL EDITAR
// ==========================================

function editarTarea(id) {
    // Fetch tarea data
    fetch(`/cliente/tareas/${id}`)
        .then(response => response.json())
        .then(tarea => {
            document.getElementById('editarTareaId').value = tarea.id;
            document.getElementById('editarTitulo').value = tarea.titulo;
            document.getElementById('editarDescripcion').value = tarea.descripcion || '';
            document.getElementById('editarPrioridad').value = tarea.prioridad;
            document.getElementById('editarEstado').value = tarea.estadoTarea;

            // Formatear fecha
            const fecha = new Date(tarea.fechaLimite);
            const fechaStr = fecha.toISOString().split('T')[0];
            document.getElementById('editarFechaLimite').value = fechaStr;

            // Mostrar modal
            const modal = new bootstrap.Modal(document.getElementById('modalEditarTarea'));
            modal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al cargar la tarea');
        });
}

// Submit del formulario de edición
document.getElementById('formEditarTarea')?.addEventListener('submit', function(e) {
    e.preventDefault();

    const formData = new FormData(this);
    const taskId = document.getElementById('editarTareaId').value;

    fetch(`/cliente/tareas/${taskId}/actualizar`, {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            alert('Error al actualizar la tarea');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al actualizar la tarea');
    });
});

// ==========================================
// MODAL ELIMINAR
// ==========================================

function confirmarEliminar(id, titulo) {
    document.getElementById('eliminarTareaId').value = id;
    document.getElementById('eliminarTareaTitulo').textContent = titulo;

    const modal = new bootstrap.Modal(document.getElementById('modalEliminarTarea'));
    modal.show();
}

// Submit del formulario de eliminación
document.getElementById('formEliminarTarea')?.addEventListener('submit', function(e) {
    const taskId = document.getElementById('eliminarTareaId').value;

    fetch(`/cliente/tareas/${taskId}/eliminar`, {
        method: 'POST'
    })
    .then(response => {
        if (response.ok) {
            location.reload();
        } else {
            alert('Error al eliminar la tarea');
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error al eliminar la tarea');
    });
});

// ==========================================
// INICIALIZACIÓN
// ==========================================

document.addEventListener('DOMContentLoaded', function() {
    initDragAndDrop();
});