const toggleButton = document.getElementById('toggle-btn')
const sidebar = document.getElementById('sidebar')
const hamburgerBtn = document.getElementById('hamburger-btn')
const sidebarOverlay = document.getElementById('sidebar-overlay')

function toggleSidebar(){
  sidebar.classList.toggle('close')
  toggleButton.classList.toggle('rotate')

  closeAllSubMenus()
}

// Função para abrir/fechar sidebar em mobile
function toggleMobileSidebar() {
  sidebar.classList.toggle('mobile-open')
  sidebarOverlay.classList.toggle('show')
}

// Event listener para o botão hambúrguer
if (hamburgerBtn) {
  hamburgerBtn.addEventListener('click', toggleMobileSidebar)
}

// Event listener para o overlay (fechar ao clicar fora)
if (sidebarOverlay) {
  sidebarOverlay.addEventListener('click', toggleMobileSidebar)
}

// Event listener para o botão de fechar dentro do sidebar (em mobile)
if (toggleButton) {
  toggleButton.addEventListener('click', () => {
    // Verifica se está em mobile (sidebar com classe mobile-open)
    if (sidebar.classList.contains('mobile-open')) {
      toggleMobileSidebar()
    } else {
      // Desktop: comportamento normal
      toggleSidebar()
    }
  })
}

function toggleSubMenu(button){

  if(!button.nextElementSibling.classList.contains('show')){
    closeAllSubMenus()
  }

  button.nextElementSibling.classList.toggle('show')
  button.classList.toggle('rotate')

  if(sidebar.classList.contains('close')){
    sidebar.classList.toggle('close')
    toggleButton.classList.toggle('rotate')
  }
}

function closeAllSubMenus(){
  Array.from(sidebar.getElementsByClassName('show')).forEach(ul => {
    ul.classList.remove('show')
    ul.previousElementSibling.classList.remove('rotate')
  })
}


const searchForm = document.querySelector('.search-form')
if (searchForm) {
  const searchInput = searchForm.querySelector('input[type="search"]')

  
  searchForm.addEventListener('submit', (e) => e.preventDefault())

  searchForm.addEventListener('click', (e) => {
    
    if (e.target && e.target.tagName && e.target.tagName.toLowerCase() === 'input') return

    if (sidebar.classList.contains('close')) {
      
      toggleSidebar()
      
      setTimeout(() => searchInput && searchInput.focus(), 350)
    } else {
      
      searchInput && searchInput.focus()
    }
  })
}

// --- LÓGICA DO TEMA CLARO / ESCURO ---

const themeBtn = document.getElementById('theme-btn');

// Função para aplicar o tema (claro ou escuro)
function setAppTheme(theme) {
  if (theme === 'light') {
    document.body.classList.add('light-mode');
  } else {
    document.body.classList.remove('light-mode');
  }
}

// Verifica se já existe um tema salvo no navegador
const savedTheme = localStorage.getItem('theme') || 'dark'; // Padrão é escuro
setAppTheme(savedTheme);


// O que acontece ao clicar no botão
if (themeBtn) {
  themeBtn.addEventListener('click', (e) => {
    e.preventDefault();
    
    // Verifica qual é o tema atual e inverte
    let newTheme = document.body.classList.contains('light-mode') ? 'dark' : 'light';
    
    // Aplica o novo tema
    setAppTheme(newTheme);
    
    // Salva a escolha no navegador
    localStorage.setItem('theme', newTheme);
  });
}
