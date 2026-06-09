/* ============================================================
   sidebarLoader.js — BuSafe (Carregamento Dinâmico do Menu)
   ============================================================ */

document.addEventListener("DOMContentLoaded", () => {
    // 1. Busca o arquivo HTML do sidebar (Liberado no seu SecurityConfig)
    fetch('/html/components/sidebar.html') 
        .then(response => {
            if (!response.ok) throw new Error('Erro ao carregar o sidebar: HTTP ' + response.status);
            return response.text();
        })
        .then(html => {
            // 2. Injeta o HTML dentro do recipiente na página atual
            const container = document.getElementById('sidebar-container');
            if (container) {
                container.innerHTML = html;
                
                // 3. Com o HTML na tela, inicializamos as lógicas e os botões
                inicializarSidebar();
            }
        })
        .catch(err => console.error('Erro no sidebarLoader:', err));
});

function inicializarSidebar() {
    // 1. Controle de acesso por perfil (TipoUsuario)
    controlarAcessoPorPerfil();

    // 2. Carrega os favoritos do usuário logado
    carregarFavoritosSidebar();

    // 3. Aciona a responsividade, cliques e tema que estão no scripts.js
    if (typeof inicializarBotoesSidebar === "function") {
        inicializarBotoesSidebar();
    }
}

// ============================================================
// CONTROLE DE ACESSO POR PERFIL (TipoUsuario Enum)
// ============================================================
function controlarAcessoPorPerfil() {
    const token = localStorage.getItem('token');
    if (!token) return;

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const tipoUsuario = payload.tipoUsuario; // "PESSOA_FISICA" ou "PESSOA_JURIDICA"
        const plano = payload.plano; // "FREE", "INDIVIDUAL", "EMPRESARIAL"

        // Menu Vinculados (apenas PESSOA_JURIDICA)
        const menuVinculados = document.getElementById('menu-vinculados');
        if (menuVinculados) {
            if (tipoUsuario === 'PESSOA_JURIDICA') {
                menuVinculados.style.display = 'flex';
            } else {
                menuVinculados.style.display = 'none';
            }
        }

        // Menu Relatórios (oculto apenas para PESSOA_FISICA com plano FREE)
        const menuRelatorios = document.getElementById('menu-relatorios');
        if (menuRelatorios) {
            if (tipoUsuario === 'PESSOA_FISICA' && plano === 'FREE') {
                menuRelatorios.style.display = 'none';
            } else {
                menuRelatorios.style.display = 'flex';
            }
        }

    } catch (error) {
        console.error('Erro ao decodificar token:', error);
        // Token inválido ou expirado
        localStorage.clear();
        window.location.href = '/login';
    }
}

// --- FUNÇÕES DE GERENCIAMENTO DE FAVORITOS (API) ---

async function carregarFavoritosSidebar() {
    const token = localStorage.getItem('token');
    const container = document.getElementById('lista-favoritos-sidebar');
    
    if (!token || !container) {
        if (container) container.innerHTML = '<span class="nav-item" style="font-size: 0.8rem; opacity: 0.5; padding-left: 2.6rem;">Faça login para ver</span>';
        return;
    }
    
    try {
        // Requisição apontando diretamente para o seu servidor Spring Boot
        const response = await fetch('http://localhost:8080/api/users/rotas-fav', {
            method: 'GET',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        
        if (response.ok) {
            const favoritos = await response.json();
            container.innerHTML = '';
            
            if (!favoritos || favoritos.length === 0) {
                container.innerHTML = '<span class="nav-item" style="font-size: 0.8rem; opacity: 0.5; padding-left: 2.6rem;">Nenhuma linha salva</span>';
                return;
            }
            
            favoritos.forEach(rota => {
                const link = document.createElement('a');
                link.href = "#";
                link.className = "nav-item";
                link.style.paddingLeft = "2.6rem"; 
                
                const numeroLinha = rota.linhaTranscol || rota.linhaMunicipal || "---";
                const nomeLinha = rota.nome || "";
                link.innerHTML = `<span>Linha ${numeroLinha} - ${nomeLinha}</span>`;
                
                link.onclick = (e) => {
                    e.preventDefault();
                    
                    // Se o usuário já estiver na tela do mapa, executa a busca direto na tela
                    if (window.location.pathname.includes('mapa.html') || window.location.pathname === '/') {
                        const campoBusca = document.getElementById('linha');
                        if (campoBusca) {
                            campoBusca.value = numeroLinha; 
                            const btnGo = document.getElementById('go');
                            if (btnGo) btnGo.click();
                        }
                        
                        // Fecha a barra lateral automaticamente se o usuário estiver no celular
                        if (window.innerWidth <= 768) {
                            const sidebar = document.getElementById('sidebar');
                            const overlay = document.getElementById('sidebar-overlay');
                            if (sidebar) sidebar.classList.remove('mobile-open');
                            if (overlay) overlay.classList.remove('show');
                        }
                    } else {
                        // Se estiver em outra tela (ex: relatórios), redireciona passando o parâmetro
                        window.location.href = `mapa.html?linha=${numeroLinha}`;
                    }
                };
                container.appendChild(link);
            });
        } else {
            container.innerHTML = '<span class="nav-item" style="font-size: 0.8rem; opacity: 0.5; padding-left: 2.6rem;">Sessão expirada</span>';
        }
    } catch (err) { 
        console.error("Erro ao carregar favoritos no sidebar:", err); 
    }
}

// --- FUNÇÕES DE LOGOUT (Acionadas pelos cliques do Sidebar) ---

function fazerLogout(event) {
    event.preventDefault();
    const modal = document.getElementById('modal-logout');
    if (modal) modal.style.display = 'flex';
}

function fecharModalLogout() {
    const modal = document.getElementById('modal-logout');
    if (modal) modal.style.display = 'none';
}

function confirmarSair() {
    localStorage.clear();
    window.location.href = "login.html"; 
}