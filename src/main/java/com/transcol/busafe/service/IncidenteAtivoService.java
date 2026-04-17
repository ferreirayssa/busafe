package com.transcol.busafe.service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IncidenteAtivoService {

    private static final String PREFIX = "incidente:";
    private static final String INDEX_KEY = "incidentes:ativos";
    private static final long TTL_MINUTES = 30;

    private final RedisTemplate<String, Object> redisTemplate;

    public IncidenteAtivoService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Cria um incidente ativo no Redis com TTL de 30 minutos.
     * Retorna o ID gerado.
     */
    public String criarIncidente(String tipo, Integer linha, String tipoLinha,
                                  double lat, double lon) {
        String id = UUID.randomUUID().toString();
        String key = PREFIX + id;

        Map<String, Object> dados = new HashMap<>();
        dados.put("id", id);
        dados.put("tipo", tipo);
        dados.put("linha", linha);
        dados.put("tipoLinha", tipoLinha);
        dados.put("criadoEm", System.currentTimeMillis());

        redisTemplate.opsForHash().putAll(key, dados);
        redisTemplate.expire(key, TTL_MINUTES, TimeUnit.MINUTES);

        // Salva a coordenada inicial
        adicionarCoordenada(id, lat, lon);

        // Adiciona ao indice de incidentes ativos
        redisTemplate.opsForSet().add(INDEX_KEY, id);
        redisTemplate.expire(INDEX_KEY, TTL_MINUTES + 5, TimeUnit.MINUTES);

        return id;
    }

    /**
     * Adiciona uma nova coordenada ao rastro do incidente.
     */
    public boolean adicionarCoordenada(String id, double lat, double lon) {
        String key = PREFIX + id;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return false;
        }

        String coordsKey = PREFIX + id + ":coords";
        Map<String, Object> coord = new HashMap<>();
        coord.put("lat", lat);
        coord.put("lon", lon);
        coord.put("ts", System.currentTimeMillis());
        redisTemplate.opsForList().rightPush(coordsKey, coord);
        redisTemplate.expire(coordsKey, TTL_MINUTES, TimeUnit.MINUTES);
        return true;
    }

    /**
     * Retorna todos os incidentes ativos com sua ultima coordenada.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarAtivos() {
        Set<Object> ids = redisTemplate.opsForSet().members(INDEX_KEY);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<Map<String, Object>> resultado = new ArrayList<>();
        List<String> idsExpirados = new ArrayList<>();

        for (Object rawId : ids) {
            String id = rawId.toString();
            String key = PREFIX + id;
            if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
                idsExpirados.add(id);
                continue;
            }

            Map<Object, Object> dados = redisTemplate.opsForHash().entries(key);
            Map<String, Object> incidente = new HashMap<>();
            incidente.put("id", id);
            incidente.put("tipo", dados.get("tipo"));
            incidente.put("linha", dados.get("linha"));
            incidente.put("tipoLinha", dados.get("tipoLinha"));
            incidente.put("criadoEm", dados.get("criadoEm"));

            // Ultima coordenada
            String coordsKey = PREFIX + id + ":coords";
            Long size = redisTemplate.opsForList().size(coordsKey);
            if (size != null && size > 0) {
                Object ultima = redisTemplate.opsForList().index(coordsKey, size - 1);
                incidente.put("coordenada", ultima);
            }

            // Todas as coordenadas (para tracar a rota percorrida)
            List<Object> todasCoords = redisTemplate.opsForList().range(coordsKey, 0, -1);
            incidente.put("coordenadas", todasCoords);

            resultado.add(incidente);
        }

        // Limpa IDs expirados do indice
        for (String id : idsExpirados) {
            redisTemplate.opsForSet().remove(INDEX_KEY, id);
        }

        return resultado;
    }

    /**
     * Remove um incidente ativo (ex: saiu da rota).
     */
    public void removerIncidente(String id) {
        redisTemplate.delete(PREFIX + id);
        redisTemplate.delete(PREFIX + id + ":coords");
        redisTemplate.opsForSet().remove(INDEX_KEY, id);
    }
}
