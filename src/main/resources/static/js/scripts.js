/* ============================================================
   scripts.js — BuSafe  (sidebar + tema)
   ============================================================ */

const sidebar        = document.getElementById('sidebar');
const toggleBtn      = document.getElementById('toggle-btn');
const hamburgerBtn   = document.getElementById('hamburger-btn');
const sidebarOverlay = document.getElementById('sidebar-overlay');

/* ---------- desktop: recolher/expandir ---------- */
function toggleSidebar() {
  sidebar.classList.toggle('close');
  closeAllSubMenus();
}

/* ---------- mobile: abrir/fechar ---------- */
function toggleMobileSidebar() {
  const opening = !sidebar.classList.contains('mobile-open');
  sidebar.classList.toggle('mobile-open');
  sidebarOverlay.classList.toggle('show');
  // garante que abre sempre expandida, mesmo se estava colapsada no desktop
  if (opening) sidebar.classList.remove('close');
}

if (hamburgerBtn) hamburgerBtn.addEventListener('click', toggleMobileSidebar);
if (sidebarOverlay) sidebarOverlay.addEventListener('click', toggleMobileSidebar);

if (toggleBtn) {
  toggleBtn.addEventListener('click', () => {
    if (sidebar.classList.contains('mobile-open')) {
      toggleMobileSidebar();
    } else {
      toggleSidebar();
    }
  });
}

/* ---------- submenu ---------- */
function toggleSubMenu(button) {
  const sub = button.nextElementSibling;
  const isOpen = sub.classList.contains('show');

  closeAllSubMenus();

  if (!isOpen) {
    sub.classList.add('show');
    button.classList.add('rotate');
  }

  if (sidebar.classList.contains('close')) {
    sidebar.classList.remove('close');
  }
}

function closeAllSubMenus() {
  sidebar.querySelectorAll('.sub-menu.show').forEach(ul => {
    ul.classList.remove('show');
    ul.previousElementSibling.classList.remove('rotate');
  });
}

const searchForm = document.querySelector('.search-form');
if (searchForm) {
  const searchInput = searchForm.querySelector('input[type="search"]');
  searchForm.addEventListener('submit', e => e.preventDefault());
  searchForm.addEventListener('click', e => {
    if (e.target?.tagName?.toLowerCase() === 'input') return;
    if (sidebar.classList.contains('close')) {
      toggleSidebar();
      setTimeout(() => searchInput?.focus(), 320);
    } else {
      searchInput?.focus();
    }
  });
}

function setTheme(theme) {
  document.body.classList.toggle('light-mode', theme === 'light');
}

setTheme(localStorage.getItem('theme') || 'dark');

const themeBtn = document.getElementById('theme-btn');
if (themeBtn) {
  themeBtn.addEventListener('click', e => {
    e.preventDefault();
    const next = document.body.classList.contains('light-mode') ? 'dark' : 'light';
    setTheme(next);
    localStorage.setItem('theme', next);
  });
}