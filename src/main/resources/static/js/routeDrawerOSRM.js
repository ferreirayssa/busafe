// ============================================================
// ROUTE DRAWER OSRM - ROTAS REAIS COM OPEN STREET MAPS
// ============================================================

const RouteDrawerOSRM = (function() {
    'use strict';
    
    // Estado privado
    let isActive = false;
    let currentRouteLayer = null;
    let currentUserLat = null;
    let currentUserLng = null;
    let mapInstance = null;
    let btnToggle = null;
    let btnClear = null;
    
    // ============================================================
    // FUNÇÕES AUXILIARES
    // ============================================================
    
    function mostrarToast(mensagem, tipo = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            document.body.appendChild(container);
        }
        
        const cores = {
            success: '#4CAF50',
            error: '#ef5350',
            warning: '#ff9800',
            info: '#10659c'
        };
        
        const icones = {
            success: 'fa-check-circle',
            error: 'fa-times-circle',
            warning: 'fa-exclamation-triangle',
            info: 'fa-info-circle'
        };
        
        const toast = document.createElement('div');
        toast.className = 'toast';
        toast.style.animation = 'slideInRight 0.3s ease';
        toast.innerHTML = `
            <div style="background: ${cores[tipo]}; color: white; padding: 12px 20px; border-radius: 8px; display: flex; align-items: center; gap: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.3);">
                <i class="fas ${icones[tipo]}"></i>
                <span>${mensagem}</span>
            </div>
        `;
        
        container.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }
    
    function formatarDistancia(metros) {
        if (metros < 1000) {
            return `${Math.round(metros)} metros`;
        } else {
            return `${(metros / 1000).toFixed(1)} km`;
        }
    }
    
    function formatarDuracao(segundos) {
        if (segundos < 60) {
            return `${Math.round(segundos)} segundos`;
        } else if (segundos < 3600) {
            const minutos = Math.round(segundos / 60);
            return `${minutos} minuto${minutos > 1 ? 's' : ''}`;
        } else {
            const horas = Math.round(segundos / 3600);
            return `${horas} hora${horas > 1 ? 's' : ''}`;
        }
    }
    
    function limparRota() {
        // Remover camada da rota
        if (currentRouteLayer && mapInstance) {
            mapInstance.removeLayer(currentRouteLayer);
            currentRouteLayer = null;
        }
        
        // Remover marcador de destino (pin vermelho)
        if (window._destPinLayer) {
            mapInstance.removeLayer(window._destPinLayer);
            window._destPinLayer = null;
        }
        
        // Desabilitar botão de limpar
        if (btnClear) {
            btnClear.disabled = true;
            btnClear.style.opacity = '0.5';
        }
    }
    
    // ============================================================
    // CRIAR PIN SVG VERMELHO
    // ============================================================
    
    function criarPinVermelho() {
        const pinSvg = `
            <svg xmlns="http://www.w3.org/2000/svg" width="36" height="46" viewBox="0 0 24 38">
                <defs>
                    <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="1" dy="2" stdDeviation="2" flood-opacity="0.4"/>
                    </filter>
                </defs>
                <path d="M12 0C5.37 0 0 5.37 0 12c0 9 12 26 12 26s12-17 12-26c0-6.63-5.37-12-12-12z" 
                      fill="#e74c3c" 
                      stroke="#ffffff" 
                      stroke-width="2.5"
                      filter="url(#shadow)"/>
                <circle cx="12" cy="12" r="4" fill="#ffffff"/>
            </svg>
        `;
        return 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(pinSvg);
    }
    
    // ============================================================
    // FUNÇÃO PRINCIPAL - DESENHAR ROTA
    // ============================================================
    
    async function desenharRota(destLat, destLng) {
        // Validar se temos localização
        if (!currentUserLat || !currentUserLng) {
            mostrarToast('📍 Aguardando localização do GPS. Ative a localização no navegador.', 'warning');
            return false;
        }
        
        if (!mapInstance) {
            console.error('Mapa não inicializado');
            return false;
        }
        
        mostrarToast('🔄 Calculando melhor rota...', 'info');
        
        try {
            // Chamar o backend
            const response = await fetch(
                `/api/mapa/rota-osrm?origemLat=${currentUserLat}&origemLng=${currentUserLng}&destinoLat=${destLat}&destinoLng=${destLng}`
            );
            
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const data = await response.json();
            
            if (!data.sucesso) {
                mostrarToast(data.mensagem || 'Não foi possível calcular a rota', 'warning');
                return false;
            }
            
            // Limpar rota anterior
            limparRota();
            
            // Converter a geometria do GeoJSON
            const geoJson = new ol.format.GeoJSON();
            const geometry = geoJson.readGeometry(JSON.parse(data.geometry));
            geometry.transform('EPSG:4326', 'EPSG:3857');
            
            // Criar feature da rota
            const routeFeature = new ol.Feature({
                geometry: geometry
            });
            
            // Estilo da rota - PRETA, CONTÍNUA, WIDTH: 4
            const routeStyle = new ol.style.Style({
                stroke: new ol.style.Stroke({
                    color: '#000000',  // Preto
                    width: 4,          // Espessura 4
                    lineDash: undefined // Sem tracejado = contínua
                })
            });
            
            // Adicionar camada da rota
            const routeSource = new ol.source.Vector({
                features: [routeFeature]
            });
            
            currentRouteLayer = new ol.layer.Vector({
                source: routeSource,
                style: routeStyle,
                zIndex: 1000
            });
            
            mapInstance.addLayer(currentRouteLayer);
            
            // Adicionar PIN VERMELHO no destino (sem pin verde na origem)
            adicionarPinDestino(destLat, destLng);
            
            // Ajustar zoom para mostrar toda a rota
            const extent = geometry.getExtent();
            const paddedExtent = ol.extent.buffer(extent, 0.1);
            mapInstance.getView().fit(paddedExtent, {
                padding: [50, 50, 50, 50],
                duration: 800,
                maxZoom: 16
            });
            
            // Mostrar informações
            const distancia = formatarDistancia(data.distancia);
            const duracao = formatarDuracao(data.duracaoSegundos);
            
            mostrarToast(`📍 Rota calculada! ${distancia} • ${duracao}`, 'success');
            
            // Habilitar botão de limpar
            if (btnClear) {
                btnClear.disabled = false;
                btnClear.style.opacity = '1';
            }
            
            return true;
            
        } catch (error) {
            console.error('Erro ao calcular rota:', error);
            mostrarToast('❌ Erro ao calcular rota. Tente novamente.', 'error');
            return false;
        }
    }
    
    function adicionarPinDestino(destLat, destLng) {
        // Coordenada do destino
        const destination = ol.proj.fromLonLat([destLng, destLat]);
        
        // Criar feature com o pin
        const pinFeature = new ol.Feature({
            geometry: new ol.geom.Point(destination)
        });
        
        // Estilo do pin vermelho SVG
        const pinStyle = new ol.style.Style({
            image: new ol.style.Icon({
                src: criarPinVermelho(),
                scale: 1,
                anchor: [0.5, 1],
                anchorXUnits: 'fraction',
                anchorYUnits: 'fraction'
            })
        });
        
        const pinLayer = new ol.layer.Vector({
            source: new ol.source.Vector({ features: [pinFeature] }),
            style: pinStyle,
            zIndex: 1001
        });
        
        mapInstance.addLayer(pinLayer);
        window._destPinLayer = pinLayer;
    }
    
    // ============================================================
    // FUNÇÕES DE CONTROLE
    // ============================================================
    
    function toggleModo() {
        isActive = !isActive;
        
        if (btnToggle) {
            if (isActive) {
                btnToggle.classList.add('active');
                btnToggle.style.background = 'linear-gradient(135deg, #e74c3c, #c0392b)';
                btnToggle.style.transform = 'scale(1.1)';
                btnToggle.title = 'Desativar Modo Rota';
                mostrarToast('✅ Modo Rota ativado! Clique em qualquer lugar do mapa para traçar a rota.', 'info');
                
                if (!currentUserLat || !currentUserLng) {
                    mostrarToast('⚠️ Aguardando localização do GPS...', 'warning');
                } else {
                    mostrarToast(`📍 Sua localização: ${currentUserLat.toFixed(4)}, ${currentUserLng.toFixed(4)}`, 'success');
                }
            } else {
                btnToggle.classList.remove('active');
                btnToggle.style.background = 'linear-gradient(135deg, #9b59b6, #8e44ad)';
                btnToggle.style.transform = 'scale(1)';
                btnToggle.title = 'Ativar Modo Rota';
                limparRota();
                mostrarToast('Modo Rota desativado', 'info');
            }
        }
    }
    
    // ============================================================
    // API PÚBLICA
    // ============================================================
    
    function init(map) {
        if (!map) {
            console.error('RouteDrawerOSRM: Mapa não fornecido');
            return false;
        }
        
        mapInstance = map;
        
        // Criar botão flutuante
        criarBotao();
        
        // Criar botão de limpar no header
        criarBotaoLimpar();
        
        // Adicionar evento de clique no mapa
        mapInstance.on('click', function(evt) {
            if (isActive) {
                const coord = ol.proj.toLonLat(evt.coordinate);
                desenharRota(coord[1], coord[0]);
            }
        });
        
        console.log('✅ RouteDrawerOSRM inicializado com sucesso!');
        return true;
    }
    
    function criarBotao() {
        if (document.getElementById('route-osrm-btn')) return;
        
        btnToggle = document.createElement('button');
        btnToggle.id = 'route-osrm-btn';
        btnToggle.innerHTML = '<i class="fas fa-route"></i>';
        btnToggle.title = 'Ativar Modo Rota (OSRM)';
        btnToggle.style.cssText = `
            position: absolute;
            bottom: 100px;
            right: 16px;
            z-index: 1000;
            width: 50px;
            height: 50px;
            border-radius: 50%;
            background: linear-gradient(135deg, #9b59b6, #8e44ad);
            border: none;
            color: white;
            cursor: pointer;
            font-size: 20px;
            transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(155, 89, 182, 0.4);
        `;
        
        btnToggle.onclick = toggleModo;
        
        const mapContainer = document.getElementById('map');
        if (mapContainer) {
            mapContainer.style.position = 'relative';
            mapContainer.appendChild(btnToggle);
        }
    }
    
    function criarBotaoLimpar() {
        const headerActions = document.querySelector('.header-actions');
        if (!headerActions) return;
        
        if (document.getElementById('route-clear-btn')) return;
        
        btnClear = document.createElement('button');
        btnClear.id = 'route-clear-btn';
        btnClear.className = 'btn-export';
        btnClear.innerHTML = '<i class="fas fa-trash-alt"></i> Limpar Rota';
        btnClear.disabled = true;
        btnClear.style.opacity = '0.5';
        btnClear.style.cursor = 'pointer';
        btnClear.style.transition = 'all 0.2s ease';
        
        btnClear.onclick = () => {
            limparRota();
            mostrarToast('Rota removida do mapa', 'info');
        };
        
        headerActions.appendChild(btnClear);
    }
    
    function atualizarLocalizacao(lat, lng) {
        currentUserLat = lat;
        currentUserLng = lng;
        console.log('📍 Localização OSRM atualizada:', lat, lng);
    }
    
    function ativar() {
        if (!isActive) toggleModo();
    }
    
    function desativar() {
        if (isActive) toggleModo();
    }
    
    function getStatus() {
        return {
            ativo: isActive,
            temLocalizacao: currentUserLat !== null && currentUserLng !== null,
            latitude: currentUserLat,
            longitude: currentUserLng
        };
    }
    
    // ============================================================
    // EXPORTAR API
    // ============================================================
    
    return {
        init: init,
        atualizarLocalizacao: atualizarLocalizacao,
        ativar: ativar,
        desativar: desativar,
        getStatus: getStatus,
        limpar: limparRota
    };
})();