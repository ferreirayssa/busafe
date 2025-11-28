const CACHE_NAME = 'busafe-v1';
const urlsToCache = [
  '/Index.html',
  '/html/Alert.html',
  '/css/Style.css',
  '/js/scripts.js',
  '/Logos/Logo_3.png',
  'https://cdn.jsdelivr.net/npm/ol@8.2.0/ol.css',
  'https://cdn.jsdelivr.net/npm/ol@8.2.0/dist/ol.js'
];

// Instalação do Service Worker
self.addEventListener('install', (event) => {
  console.log('[Service Worker] Instalando...');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('[Service Worker] Cache aberto');
        return cache.addAll(urlsToCache);
      })
      .catch((error) => {
        console.error('[Service Worker] Erro ao cachear:', error);
      })
  );
  self.skipWaiting();
});

// Ativação do Service Worker
self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Ativando...');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            console.log('[Service Worker] Removendo cache antigo:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  return self.clients.claim();
});

// Interceptação de requisições
self.addEventListener('fetch', (event) => {
  // Ignora requisições que não são GET
  if (event.request.method !== 'GET') {
    return;
  }

  // Ignora requisições para APIs externas que precisam estar sempre atualizadas
  if (event.request.url.includes('/api/') || 
      event.request.url.includes('/rotas/') ||
      event.request.url.includes('/relatos')) {
    // Network first para APIs
    event.respondWith(
      fetch(event.request)
        .catch(() => {
          return new Response(
            JSON.stringify({ error: 'Sem conexão com a internet' }),
            { headers: { 'Content-Type': 'application/json' } }
          );
        })
    );
    return;
  }

  // Cache first para recursos estáticos
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        if (response) {
          console.log('[Service Worker] Servindo do cache:', event.request.url);
          return response;
        }

        console.log('[Service Worker] Buscando da rede:', event.request.url);
        return fetch(event.request)
          .then((response) => {
            // Verifica se a resposta é válida
            if (!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }

            // Clona a resposta para cachear
            const responseToCache = response.clone();

            caches.open(CACHE_NAME)
              .then((cache) => {
                cache.put(event.request, responseToCache);
              });

            return response;
          })
          .catch((error) => {
            console.error('[Service Worker] Erro ao buscar:', error);
            // Retorna uma página offline personalizada se disponível
            return caches.match('/Index.html');
          });
      })
  );
});

// Sincronização em background (opcional)
self.addEventListener('sync', (event) => {
  console.log('[Service Worker] Sincronização em background:', event.tag);
  if (event.tag === 'sync-reports') {
    event.waitUntil(syncReports());
  }
});

// Função para sincronizar relatórios offline
async function syncReports() {
  try {
    // Aqui você pode implementar lógica para enviar relatórios salvos offline
    console.log('[Service Worker] Sincronizando relatórios...');
  } catch (error) {
    console.error('[Service Worker] Erro ao sincronizar:', error);
  }
}

// Notificações push (opcional para futuras implementações)
self.addEventListener('push', (event) => {
  const options = {
    body: event.data ? event.data.text() : 'Nova notificação do BuSafe',
    icon: '/Logos/Logo_3.png',
    badge: '/Logos/Logo_3.png',
    vibrate: [200, 100, 200],
    tag: 'busafe-notification'
  };

  event.waitUntil(
    self.registration.showNotification('BuSafe', options)
  );
});
