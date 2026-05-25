/**
 * sidebar.js - Lógica reutilizable para el menú hamburguesa
 * Funciona en home, tareas, calendario y cualquier página con el mismo HTML
 */

document.addEventListener('DOMContentLoaded', function() {
    const menuToggle = document.getElementById('menuToggle');
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('sidebarOverlay');
    const body = document.body;

    // Funciones para abrir/cerrar sidebar
    function openSidebar() {
        if (sidebar) sidebar.classList.add('show');
        if (overlay) overlay.classList.add('show');
        if (menuToggle) menuToggle.setAttribute('aria-expanded', 'true');
        if (body) body.style.overflow = 'hidden';
    }

    function closeSidebar() {
        if (sidebar) sidebar.classList.remove('show');
        if (overlay) overlay.classList.remove('show');
        if (menuToggle) menuToggle.setAttribute('aria-expanded', 'false');
        if (body) body.style.overflow = '';
    }

    // Toggle con botón hamburguesa
    if (menuToggle) {
        menuToggle.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            if (sidebar && sidebar.classList.contains('show')) {
                closeSidebar();
            } else {
                openSidebar();
            }
        });
    }

    // Cerrar al hacer clic en el overlay
    if (overlay) {
        overlay.addEventListener('click', function(e) {
            e.preventDefault();
            closeSidebar();
        });
    }

    // Cerrar al hacer clic en un enlace del sidebar (móvil)
    if (sidebar) {
        sidebar.querySelectorAll('.nav-link, .categoria-link').forEach(link => {
            link.addEventListener('click', function() {
                if (window.innerWidth < 992) {
                    closeSidebar();
                }
            });
        });
    }

    // Cerrar con tecla ESC
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && sidebar && sidebar.classList.contains('show')) {
            closeSidebar();
        }
    });

    // Cerrar automáticamente al redimensionar a desktop
    window.addEventListener('resize', function() {
        if (window.innerWidth >= 992 && sidebar && sidebar.classList.contains('show')) {
            closeSidebar();
        }
    });
});