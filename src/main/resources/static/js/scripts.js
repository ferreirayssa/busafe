/* ============================================================
   scripts.js — BuSafe  (sidebar + tema)
   ============================================================ */

// Declaramos as variáveis soltas para que todas as funções possam acessá-las
let sidebar;
let toggleBtn;
let hamburgerBtn;
let sidebarOverlay;

// Essa função será chamada pelo sidebarLoader.js assim que o HTML nascer na tela!
function inicializarBotoesSidebar() {
  sidebar        = document.getElementById('sidebar');
  toggleBtn      = document.getElementById('toggle-btn');
  hamburgerBtn   = document.getElementById('hamburger-btn');
  sidebarOverlay = document.getElementById('sidebar-overlay');

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

  // --- BUSCA DA SIDEBAR ---
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

  // --- BOTÃO DO TEMA ---
  const themeBtn = document.getElementById('theme-btn');
  if (themeBtn) {
    themeBtn.addEventListener('click', e => {
      e.preventDefault();
      const next = document.body.classList.contains('light-mode') ? 'dark' : 'light';
      setTheme(next);
      localStorage.setItem('theme', next);
    });
  }
}


/* ---------- desktop: recolher/expandir ---------- */
function toggleSidebar() {
  if (!sidebar) return;
  sidebar.classList.toggle('close');
  closeAllSubMenus();
}

/* ---------- mobile: abrir/fechar ---------- */
function toggleMobileSidebar() {
  if (!sidebar || !sidebarOverlay) return;
  
  const opening = !sidebar.classList.contains('mobile-open');
  sidebar.classList.toggle('mobile-open');
  sidebarOverlay.classList.toggle('show');
  
  // garante que abre sempre expandida, mesmo se estava colapsada no desktop
  if (opening) sidebar.classList.remove('close');
}

/* ---------- submenu ---------- */
function toggleSubMenu(button) {
  if (!sidebar) sidebar = document.getElementById('sidebar'); // Garante que a variável exista se chamada direto do HTML
  
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
  if (!sidebar) return;
  sidebar.querySelectorAll('.sub-menu.show').forEach(ul => {
    ul.classList.remove('show');
    ul.previousElementSibling.classList.remove('rotate');
  });
}

/* ---------- tema (Roda imediatamente para a tela não piscar branco/preto) ---------- */
function setTheme(theme) {
  document.body.classList.toggle('light-mode', theme === 'light');
}

setTheme(localStorage.getItem('theme') || 'dark');