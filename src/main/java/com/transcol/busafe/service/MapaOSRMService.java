package com.transcol.busafe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class MapaOSRMService {

    @Autowired
    private RestTemplate restTemplate;

    private static final String OSRM_URL = "http://router.project-osrm.org/route/v1/driving/";

    public Map<String, Object> calcularRota(double origemLat, double origemLng, double destinoLat, double destinoLng) {
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Montar URL da OSRM (atenção: longitude primeiro, depois latitude)
            String url = String.format(
                "%s%f,%f;%f,%f?overview=full&geometries=geojson",
                OSRM_URL, origemLng, origemLat, destinoLng, destinoLat
            );
            
            System.out.println("📡 Chamando OSRM: " + url);
            
            // Chamar API OSRM
            String response = restTemplate.getForObject(url, String.class);
            
            // Parse do JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response);
            
            if (json.has("routes") && json.get("routes").size() > 0) {
                JsonNode route = json.get("routes").get(0);
                JsonNode geometry = route.get("geometry");
                JsonNode legs = route.get("legs").get(0);
                
                double distancia = legs.get("distance").asDouble();
                double duracao = legs.get("duration").asDouble();
                
                resultado.put("sucesso", true);
                resultado.put("distancia", Math.round(distancia));
                resultado.put("distanciaKm", Math.round(distancia / 1000.0 * 10) / 10.0);
                resultado.put("duracaoSegundos", Math.round(duracao));
                resultado.put("duracaoMinutos", Math.round(duracao / 60));
                resultado.put("geometry", geometry.toString());
                resultado.put("origem", Map.of("lat", origemLat, "lng", origemLng));
                resultado.put("destino", Map.of("lat", destinoLat, "lng", destinoLng));
            } else {
                resultado.put("sucesso", false);
                resultado.put("mensagem", "Nenhuma rota encontrada");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            resultado.put("sucesso", false);
            resultado.put("erro", "Erro ao calcular rota: " + e.getMessage());
        }
        
        return resultado;
    }
}