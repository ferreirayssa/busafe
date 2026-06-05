console.log("Iniciando o mapa da Landing Page (Pontos Fixos + Relatos)...");

// 1. Configuração do Centro do Mapa
const mapCenter = ol.proj.fromLonLat([-40.3196, -20.3164]); 

// 2. SVG do Ponto de Ônibus e Camada Vetorial
const busIconSvg = `
  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 32 32">
    <circle cx="16" cy="16" r="14" fill="#ffffff" stroke="#2b90d9" stroke-width="2.5" />
    <svg x="8" y="8" width="16" height="16" viewBox="0 0 16 16">
      <path fill="#2b90d9" d="M16 7a1 1 0 0 1-1 1v3.5c0 .818-.393 1.544-1 2v2a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1-.5-.5V14H5v1.5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1-.5-.5v-2a2.5 2.5 0 0 1-1-2V8a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1V2.64C1 1.452 1.845.408 3.064.268A44 44 0 0 1 8 0c2.1 0 3.792.136 4.936.268C14.155.408 15 1.452 15 2.64V4a1 1 0 0 1 1 1zM3.552 3.22A43 43 0 0 1 8 3c1.837 0 3.353.107 4.448.22a.5.5 0 0 0 .104-.994A44 44 0 0 0 8 2c-1.876 0-3.426.109-4.552.226a.5.5 0 1 0 .104.994M8 4c-1.876 0-3.426.109-4.552.226A.5.5 0 0 0 3 4.723v3.554a.5.5 0 0 0 .448.497C4.574 8.891 6.124 9 8 9s3.426-.109 4.552-.226A.5.5 0 0 0 13 8.277V4.723a.5.5 0 0 0-.448-.497A44 44 0 0 0 8 4m-3 7a1 1 0 1 0-2 0 1 1 0 0 0 2 0m8 0a1 1 0 1 0-2 0 1 1 0 0 0 2 0m-7 0a1 1 0 0 0 1 1h2a1 1 0 1 0 0-2H7a1 1 0 0 0-1 1"/>
    </svg>
  </svg>
`;

const pontosSource = new ol.source.Vector();

const pontosLayer = new ol.layer.Vector({ 
  source: pontosSource,
  visible: true, // Deixei como true para aparecer na Landing Page
  style: new ol.style.Style({
    image: new ol.style.Icon({
      src: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(busIconSvg),
      scale: 0.65,
      anchor: [0.5, 0.5]
    })
  }),
  zIndex: 1
});

// 3. Inicialização do Mapa
const map = new ol.Map({
  target: 'map',
  layers: [
    new ol.layer.Tile({ source: new ol.source.OSM() }),
    pontosLayer // Adicionando a camada vetorial dos pontos
  ],
  view: new ol.View({
    center: mapCenter,
    zoom: 13
  })
});

// 4. Configuração do Popup
const popupEl = document.createElement('div');
popupEl.className = 'ol-popup';
popupEl.style.cssText = 'position: absolute; background: white; padding: 15px; border-radius: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); display: none; bottom: 35px; left: -100px; min-width: 240px; font-family: "Poppins", sans-serif; z-index: 2000;';

const popup = new ol.Overlay({
  element: popupEl,
  positioning: 'bottom-center',
  stopEvent: true,
});
map.addOverlay(popup);

// 5. Configuração dos Relatos Comunitários (Mantém o Overlay para os efeitos CSS funcionarem)
const incidentTypes = {
    'assalto': { label: 'Assalto', icon: 'fas fa-hand-holding-dollar', color: '#ef4444' },
    'assedio': { label: 'Assédio', icon: 'fas fa-triangle-exclamation', color: '#f59e0b' },
    'violencia': { label: 'Violência', icon: 'fas fa-user-slash', color: '#9333ea' },
    'furto': { label: 'Furto', icon: 'fas fa-briefcase', color: '#3b82f6' },
    'quebrado': { label: 'Ônibus Quebrado', icon: 'fas fa-bus', color: '#64748b' },
    'outro': { label: 'Outro', icon: 'fas fa-question', color: '#45505d' }
};

function adicionarMarcadorRelato(lat, lon, tipo, dados) {
    const markerDiv = document.createElement('div');
    markerDiv.className = 'marker-pin';
    markerDiv.style.cursor = 'pointer';

    const config = incidentTypes[tipo] || incidentTypes['outro'];
    markerDiv.style.background = config.color;
    markerDiv.innerHTML = `<i class="${config.icon}"></i>`;

    markerDiv.addEventListener('click', (e) => {
        e.stopPropagation(); // Evita que o clique feche o popup acidentalmente
        
        popupEl.innerHTML = `
            <div style="border-bottom: 3px solid ${config.color}; padding-bottom: 8px; margin-bottom: 8px;">
                <span style="font-size: 0.75rem; color: #888; text-transform: uppercase; letter-spacing: 1px; display: block;">Relato Comunitário</span>
                <strong style="color: ${config.color}; font-size: 1.1rem;">${config.label}</strong>
            </div>
            <div style="font-size: 0.9rem; color: #45505d;">
                <p style="margin-bottom: 5px;"><strong>Bairro:</strong> ${dados.bairro}</p>
                <p><strong>Detalhes:</strong> ${dados.descricao}</p>
            </div>
        `;
        popup.setPosition(ol.proj.fromLonLat([lon, lat]));
        popupEl.style.display = 'block';
    });

    const markerOverlay = new ol.Overlay({
        element: markerDiv,
        positioning: 'bottom-center',
        stopEvent: true,
        position: ol.proj.fromLonLat([lon, lat])
    });

    map.addOverlay(markerOverlay);
}

// 6. Lógica de Clique Unificada (Trata os Pontos Vetoriais e Fechamento de Popup)
map.on('click', function(evt) {
    let hitPonto = null;

    // Verifica se clicou num ponto da camada vetorial (Pontos de Ônibus)
    map.forEachFeatureAtPixel(evt.pixel, function(feature, layer) {
        if (layer === pontosLayer) {
            hitPonto = feature;
            return true; // Para o loop ao encontrar
        }
    });

    if (hitPonto) {
        // Se clicou num Ponto de Ônibus, monta e exibe o popup
        const nome = hitPonto.get('nome');
        const linhas = hitPonto.get('linhas');
        
        let linhasFormatadas = linhas.map(l => 
            `<span style="background: #eef2f5; color: #18679d; padding: 2px 6px; border-radius: 4px; font-weight: 600; font-size: 0.8rem; margin-right: 5px; display: inline-block; margin-bottom: 5px;">${l}</span>`
        ).join('');

        popupEl.innerHTML = `
            <div style="border-bottom: 3px solid #18679d; padding-bottom: 8px; margin-bottom: 8px;">
                <span style="font-size: 0.75rem; color: #888; text-transform: uppercase; letter-spacing: 1px; display: block;">Parada de Ônibus</span>
                <strong style="color: #18679d; font-size: 1.1rem;">Ponto ${nome}</strong>
            </div>
            <div style="font-size: 0.9rem; color: #45505d;">
                <p style="margin-bottom: 5px;"><strong>Linhas Atendidas:</strong></p>
                <div style="max-height: 120px; overflow-y: auto; padding-right: 5px;">${linhasFormatadas}</div>
            </div>
        `;
        popup.setPosition(evt.coordinate);
        popupEl.style.display = 'block';
    } else {
        // Se clicou num lugar vazio do mapa, fecha o popup
        popupEl.style.display = 'none';
    }
});

// Cursor de "mãozinha" ao passar sobre os Pontos de Ônibus
map.on('pointermove', function(e) {
    const hit = map.hasFeatureAtPixel(e.pixel, { layerFilter: l => l === pontosLayer });
    map.getTargetElement().style.cursor = hit ? 'pointer' : '';
});

// 7. Dados Reais Mockados
const pontosMock = [
    { lat: -20.3170426845619, lon: -40.3196039018834, nome: "110001", linhas: ["524", "531", "532", "536", "548", "561", "569"] },
    { lat: -20.3164543118005, lon: -40.3198290398441, nome: "110002", linhas: ["524", "531", "532", "536", "548", "569", "331", "333"] },
    { lat: -20.3143468773637, lon: -40.3202034975945, nome: "110004", linhas: ["524", "532", "548", "561", "569", "333"] },
    { lat: -20.3139695719922, lon: -40.3205906751802, nome: "110005", linhas: ["506", "516", "529", "531", "532", "548", "561", "569", "010", "102", "104", "161", "163", "171", "172", "173", "175", "331", "333"] }
];

// Dados Mockados de Relatos da Comunidade (Ajustados próximos à sua região)
const relatosMock = [
    { 
        lat: -20.3155, 
        lon: -40.3180, 
        tipo: 'assalto', 
        bairro: 'Maruípe', 
        descricao: 'Relato de assalto à mão armada registrado próximo ao Hospital Santa Rita.' 
    },
    { 
        lat: -20.3128, 
        lon: -40.3210, 
        tipo: 'quebrado', 
        bairro: 'Tabuazeiro', 
        descricao: 'Ônibus da linha 561 quebrou na subida, ocupando a faixa da direita.' 
    },
    { 
        lat: -20.2975, 
        lon: -40.3150, 
        tipo: 'furto', 
        bairro: 'Maruípe', 
        descricao: 'Furto de carteira por distração dentro do coletivo lotado próximo à UFES.' 
    },
    { 
        lat: -20.3080, 
        lon: -40.3120, 
        tipo: 'assalto', 
        bairro: 'Itararé', 
        descricao: 'Abordagem suspeita de dois indivíduos no ponto de ônibus em frente ao supermercado.' 
    },
    { 
        lat: -20.3015, 
        lon: -40.3050, 
        tipo: 'assedio', 
        bairro: 'Santa Luíza', 
        descricao: 'Denúncia de importunação sexual dentro do ônibus da linha 501 sentido Vila Velha.' 
    },
    { 
        lat: -20.3201, 
        lon: -40.3395, 
        tipo: 'furto', 
        bairro: 'Centro', 
        descricao: 'Furto de telemóvel no ponto de ônibus da Praça Oito durante o horário de pico.' 
    },
    { 
        lat: -20.2850, 
        lon: -40.2910, 
        tipo: 'violencia', 
        bairro: 'Jardim da Penha', 
        descricao: 'Discussão acalmada e agressão verbal entre passageiros na paragem da Av. Dante Michelini.' 
    },
    { 
        lat: -20.3255, 
        lon: -40.3540, 
        tipo: 'outro', 
        bairro: 'Vila Rubim', 
        descricao: 'Ponto de paragem com iluminação completamente apagada, gerando grande sensação de insegurança.' 
    }
];

// 8. Renderização Controlada
setTimeout(() => {
    map.updateSize();
    
    // Injeta os pontos na Camada Vetorial
    pontosMock.forEach(ponto => {
        const feature = new ol.Feature({
            geometry: new ol.geom.Point(ol.proj.fromLonLat([ponto.lon, ponto.lat])),
            nome: ponto.nome,
            linhas: ponto.linhas
        });
        pontosSource.addFeature(feature);
    });
    
    // Injeta os relatos como Overlays (para usar CSS)
    relatosMock.forEach(relato => adicionarMarcadorRelato(relato.lat, relato.lon, relato.tipo, relato));
    
}, 250);