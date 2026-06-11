package com.transcol.busafe.service;

import com.transcol.busafe.model.Relato;
import com.transcol.busafe.model.User;
import com.transcol.busafe.model.enums.TipoUsuario;
import com.transcol.busafe.repository.RelatoRepository;
import com.transcol.busafe.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    @Autowired
    private RelatoRepository relatoRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private HashService hashService;

    // ============================================================
    // MÉTODO AUXILIAR - Obter hash do usuário
    // ============================================================
    
    private String getUsuarioHash(User user) {
        return hashService.gerarHash(user.getId());
    }
    
    private List<String> getHashesUsuarios(User user) {
        List<String> hashes = new ArrayList<>();
        hashes.add(getUsuarioHash(user));
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            List<User> vinculados = userRepository.listarPorCnpjAtivos(user.getCnpj());
            vinculados.forEach(v -> hashes.add(getUsuarioHash(v)));
        }
        
        return hashes;
    }

    // ============================================================
    // relatoToMap - Converter Relato para Map
    // ============================================================
    
    private Map<String, Object> relatoToMap(Relato relato) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", relato.getId());
        map.put("tipo", relato.getTipo());
        map.put("descricao", relato.getDescricao());
        map.put("dataRelato", relato.getDataRelato() != null ? 
                relato.getDataRelato().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        map.put("latitude", relato.getLatitude());
        map.put("longitude", relato.getLongitude());
        map.put("municipio", relato.getMunicipio());
        map.put("bairro", relato.getBairro());
        map.put("linhaTranscol", relato.getLinhaTranscol());
        map.put("linhaMunicipal", relato.getLinhaMunicipal());
        return map;
    }

    // ============================================================
    // MÉTODOS AUXILIARES DE ESTATÍSTICAS
    // ============================================================

    private Map<String, Long> getRelatosPorMes(List<Relato> relatos) {
        LocalDateTime seisMesesAtras = LocalDateTime.now().minusMonths(6);
        
        return relatos.stream()
                .filter(r -> r.getDataRelato() != null && r.getDataRelato().isAfter(seisMesesAtras))
                .collect(Collectors.groupingBy(
                    r -> r.getDataRelato().getYear() + "-" + 
                         String.format("%02d", r.getDataRelato().getMonthValue()),
                    TreeMap::new,
                    Collectors.counting()
                ));
    }

    private double calcularMediaDiaria(List<Relato> relatos) {
        if (relatos.isEmpty()) return 0.0;
        
        Optional<LocalDateTime> primeiro = relatos.stream()
                .map(Relato::getDataRelato)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo);
                
        Optional<LocalDateTime> ultimo = relatos.stream()
                .map(Relato::getDataRelato)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);
        
        if (primeiro.isPresent() && ultimo.isPresent()) {
            long dias = java.time.Duration.between(primeiro.get(), ultimo.get()).toDays() + 1;
            return Math.round((double) relatos.size() / dias * 100.0) / 100.0;
        }
        
        return relatos.size();
    }

    private String getDiaMaisAtivo(List<Relato> relatos) {
        if (relatos.isEmpty()) return "N/A";
        
        return relatos.stream()
                .filter(r -> r.getDataRelato() != null)
                .collect(Collectors.groupingBy(
                    r -> r.getDataRelato().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " relatos)")
                .orElse("N/A");
    }

    private String getTipoMaisFrequente(Map<String, Long> porTipo) {
        if (porTipo == null || porTipo.isEmpty()) return "N/A";
        
        return porTipo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .orElse("N/A");
    }

    private String getMunicipioMaisFrequente(Map<String, Long> porMunicipio) {
        if (porMunicipio == null || porMunicipio.isEmpty()) return "N/A";
        
        return porMunicipio.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    private Map<String, Object> calcularTendencias(List<Relato> relatos) {
        Map<String, Object> tendencias = new HashMap<>();
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioMesAtual = agora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime inicioMesAnterior = inicioMesAtual.minusMonths(1);
        
        long totalMesAtual = relatos.stream()
                .filter(r -> r.getDataRelato() != null && !r.getDataRelato().isBefore(inicioMesAtual))
                .count();
                
        long totalMesAnterior = relatos.stream()
                .filter(r -> r.getDataRelato() != null && 
                         !r.getDataRelato().isBefore(inicioMesAnterior) && 
                         r.getDataRelato().isBefore(inicioMesAtual))
                .count();
        
        tendencias.put("totalMesAtual", totalMesAtual);
        tendencias.put("totalMesAnterior", totalMesAnterior);
        tendencias.put("mesAtual", inicioMesAtual.getMonth().toString());
        tendencias.put("mesAnterior", inicioMesAnterior.getMonth().toString());
        
        if (totalMesAnterior > 0) {
            double variacao = ((double) (totalMesAtual - totalMesAnterior) / totalMesAnterior) * 100;
            tendencias.put("variacaoPercentual", Math.round(variacao * 100.0) / 100.0);
            tendencias.put("tendencia", variacao > 0 ? "AUMENTO" : variacao < 0 ? "REDUCAO" : "ESTAVEL");
        } else {
            tendencias.put("variacaoPercentual", totalMesAtual > 0 ? 100.0 : 0.0);
            tendencias.put("tendencia", totalMesAtual > 0 ? "NOVO" : "SEM_DADOS");
        }
        
        return tendencias;
    }

    // ============================================================
    // MÉTODO EXISTENTE - GERAR RELATÓRIO COMPLETO
    // ============================================================
    
    public Map<String, Object> gerarRelatorioRelatos(User user) {
        Map<String, Object> relatorio = new HashMap<>();
        
        relatorio.put("geradoEm", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        relatorio.put("solicitante", user.getNome());
        relatorio.put("tipoUsuario", user.getTipoUsuario().name());
        relatorio.put("plano", user.getPlano().name());
        
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            relatorio.put("cnpj", user.getCnpj());
            List<User> vinculados = userRepository.listarPorCnpjAtivos(user.getCnpj());
            relatorio.put("totalVinculados", vinculados.size());
            
            Map<String, Object> relatorioEmpresarial = gerarRelatorioEmpresarial(user);
            relatorio.put("relatorioEmpresarial", relatorioEmpresarial);
        }
        
        Map<String, Object> relatorioIndividual = gerarRelatorioIndividual(user);
        relatorio.put("relatorioIndividual", relatorioIndividual);
        
        return relatorio;
    }

    private Map<String, Object> gerarRelatorioIndividual(User user) {
        Map<String, Object> individual = new HashMap<>();
        
        String usuarioHash = getUsuarioHash(user);
        List<Relato> meusRelatos = relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(usuarioHash);
        
        individual.put("totalRelatos", meusRelatos.size());
        
        Map<String, Long> porTipo = meusRelatos.stream()
                .collect(Collectors.groupingBy(Relato::getTipo, Collectors.counting()));
        individual.put("relatosPorTipo", porTipo);
        
        List<Map<String, Object>> ultimosRelatos = meusRelatos.stream()
                .limit(5)
                .map(this::relatoToMap)
                .collect(Collectors.toList());
        individual.put("ultimosRelatos", ultimosRelatos);
        
        Map<String, Long> porMes = getRelatosPorMes(meusRelatos);
        individual.put("relatosPorMes", porMes);
        
        Map<String, Long> porMunicipio = meusRelatos.stream()
                .filter(r -> r.getMunicipio() != null)
                .collect(Collectors.groupingBy(Relato::getMunicipio, Collectors.counting()));
        individual.put("relatosPorMunicipio", porMunicipio);
        
        Map<String, Long> topBairros = meusRelatos.stream()
                .filter(r -> r.getBairro() != null)
                .collect(Collectors.groupingBy(Relato::getBairro, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        individual.put("topBairros", topBairros);
        
        Map<String, Long> porLinha = new HashMap<>();
        meusRelatos.forEach(r -> {
            if (r.getLinhaTranscol() != null) {
                porLinha.merge("Transcol " + r.getLinhaTranscol(), 1L, Long::sum);
            }
            if (r.getLinhaMunicipal() != null) {
                porLinha.merge("Municipal " + r.getLinhaMunicipal(), 1L, Long::sum);
            }
        });
        individual.put("relatosPorLinha", porLinha);
        
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("mediaDiaria", calcularMediaDiaria(meusRelatos));
        estatisticas.put("diaMaisAtivo", getDiaMaisAtivo(meusRelatos));
        estatisticas.put("tipoMaisReportado", getTipoMaisFrequente(porTipo));
        estatisticas.put("municipioMaisReportado", getMunicipioMaisFrequente(porMunicipio));
        individual.put("estatisticas", estatisticas);
        
        return individual;
    }

    private Map<String, Object> gerarRelatorioEmpresarial(User empresa) {
        Map<String, Object> empresarial = new HashMap<>();
        
        List<User> vinculados = userRepository.listarPorCnpjAtivos(empresa.getCnpj());
        List<String> todosHashes = getHashesUsuarios(empresa);
        
        List<Relato> todosRelatos = new ArrayList<>();
        for (String hash : todosHashes) {
            todosRelatos.addAll(relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(hash));
        }
        todosRelatos.sort((r1, r2) -> r2.getDataRelato().compareTo(r1.getDataRelato()));
        
        empresarial.put("totalUsuarios", vinculados.size() + 1);
        empresarial.put("totalRelatos", todosRelatos.size());
        
        Map<String, Long> relatosPorUsuario = new LinkedHashMap<>();
        relatosPorUsuario.put(empresa.getNome(), relatoRepository.countByUsuarioHash(getUsuarioHash(empresa)));
        vinculados.forEach(v -> relatosPorUsuario.put(v.getNome(), relatoRepository.countByUsuarioHash(getUsuarioHash(v))));
        empresarial.put("relatosPorUsuario", relatosPorUsuario);
        
        Map<String, Long> porTipo = todosRelatos.stream()
                .collect(Collectors.groupingBy(Relato::getTipo, Collectors.counting()));
        empresarial.put("relatosPorTipo", porTipo);
        
        Map<String, Long> porMes = getRelatosPorMes(todosRelatos);
        empresarial.put("relatosPorMes", porMes);
        
        Map<String, Long> porMunicipio = todosRelatos.stream()
                .filter(r -> r.getMunicipio() != null)
                .collect(Collectors.groupingBy(Relato::getMunicipio, Collectors.counting()));
        empresarial.put("relatosPorMunicipio", porMunicipio);
        
        List<Map<String, Object>> topUsuarios = relatosPorUsuario.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("nome", entry.getKey());
                    map.put("total", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        empresarial.put("topUsuariosAtivos", topUsuarios);
        
        List<Map<String, Object>> distribuicaoGeografica = porMunicipio.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("municipio", entry.getKey());
                    map.put("total", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        empresarial.put("distribuicaoGeografica", distribuicaoGeografica);
        
        Map<String, Object> tendencias = calcularTendencias(todosRelatos);
        empresarial.put("tendencias", tendencias);
        
        return empresarial;
    }

public Map<String, Object> getEstatisticasDashboard(User user) {
    Map<String, Object> stats = new HashMap<>();
    
    String usuarioHash = getUsuarioHash(user);
    
    // Busca por hash
    List<Relato> relatosComHash = relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(usuarioHash);
    
    // Se não encontrou, busca todos (fallback para relatos antigos sem hash)
    if (relatosComHash == null || relatosComHash.isEmpty()) {
        relatosComHash = relatoRepository.findAll();
    }
    
    stats.put("totalOcorrencias", relatosComHash.size());
    
    // Tipo mais comum
    Map<String, Long> porTipo = relatosComHash.stream()
            .filter(r -> r.getTipo() != null)
            .collect(Collectors.groupingBy(Relato::getTipo, Collectors.counting()));
    
    String tipoMaisComum = porTipo.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Nenhum");
    stats.put("tipoMaisComum", tipoMaisComum);
    stats.put("mediaMensal", relatosComHash.size());
    
    Map<String, Object> tendencia = new HashMap<>();
    tendencia.put("mesAtual", relatosComHash.size());
    tendencia.put("mesAnterior", 0);
    stats.put("tendencia", tendencia);
    
    return stats;
}

    /**
     * Filtrar relatos
     */
    public Map<String, Object> filtrarRelatos(User user, Map<String, Object> filtros) {
        Map<String, Object> resultado = new HashMap<>();
        
        List<String> todosHashes = getHashesUsuarios(user);
        
        List<Relato> relatos = new ArrayList<>();
        for (String hash : todosHashes) {
            List<Relato> relatosDoHash = relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(hash);
            if (relatosDoHash != null) {
                relatos.addAll(relatosDoHash);
            }
        }
        
        System.out.println("🔍 Filtrando " + relatos.size() + " relatos");
        
        resultado.put("total", relatos.size());
        resultado.put("relatos", relatos.stream()
                .limit(50)
                .map(this::relatoToMap)
                .collect(Collectors.toList()));
        
        return resultado;
    }

    /**
     * Relatos por tipo
     */
    public List<Map<String, Object>> getRelatosPorTipo(User user, String tipo) {
        List<String> todosHashes = getHashesUsuarios(user);
        
        List<Relato> relatos = new ArrayList<>();
        for (String hash : todosHashes) {
            List<Relato> relatosDoHash = relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(hash);
            if (relatosDoHash != null) {
                relatos.addAll(relatosDoHash);
            }
        }
        
        return relatos.stream()
                .filter(r -> r.getTipo() != null && r.getTipo().equalsIgnoreCase(tipo))
                .limit(100)
                .map(this::relatoToMap)
                .collect(Collectors.toList());
    }

    /**
     * Top locais
     */
    public List<Map<String, Object>> getTopLocais(User user) {
        List<String> todosHashes = getHashesUsuarios(user);
        
        List<Relato> relatos = new ArrayList<>();
        for (String hash : todosHashes) {
            List<Relato> relatosDoHash = relatoRepository.findByUsuarioHashOrderByDataRelatoDesc(hash);
            if (relatosDoHash != null) {
                relatos.addAll(relatosDoHash);
            }
        }
        
        Map<String, Long> topBairros = relatos.stream()
                .filter(r -> r.getBairro() != null && !r.getBairro().isEmpty())
                .collect(Collectors.groupingBy(Relato::getBairro, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        Map<String, Long> topMunicipios = relatos.stream()
                .filter(r -> r.getMunicipio() != null && !r.getMunicipio().isEmpty())
                .collect(Collectors.groupingBy(Relato::getMunicipio, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        Map<String, Object> bairros = new HashMap<>();
        bairros.put("titulo", "Top Bairros");
        bairros.put("dados", topBairros);
        resultado.add(bairros);
        
        Map<String, Object> municipios = new HashMap<>();
        municipios.put("titulo", "Top Municípios");
        municipios.put("dados", topMunicipios);
        resultado.add(municipios);
        
        return resultado;
    }
}