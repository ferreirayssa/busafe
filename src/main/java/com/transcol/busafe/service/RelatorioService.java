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

    /**
     * Gera relatório completo de relatos para o usuário
     */
    public Map<String, Object> gerarRelatorioRelatos(User user) {
        Map<String, Object> relatorio = new HashMap<>();
        
        // Cabeçalho do relatório
        relatorio.put("geradoEm", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        relatorio.put("solicitante", user.getNome());
        relatorio.put("tipoUsuario", user.getTipoUsuario().name());
        relatorio.put("plano", user.getPlano().name());
        
        // Se for PESSOA_JURIDICA, inclui dados da empresa e vinculados
        if (TipoUsuario.PESSOA_JURIDICA.equals(user.getTipoUsuario())) {
            relatorio.put("cnpj", user.getCnpj());
            
            // CORRIGIDO: usando o novo nome do método
            List<User> vinculados = userRepository.listarPorCnpjAtivos(user.getCnpj());
            relatorio.put("totalVinculados", vinculados.size());
            
            // Relatório consolidado da empresa (todos os vinculados)
            Map<String, Object> relatorioEmpresarial = gerarRelatorioEmpresarial(user);
            relatorio.put("relatorioEmpresarial", relatorioEmpresarial);
        }
        
        // Relatório individual do usuário
        Map<String, Object> relatorioIndividual = gerarRelatorioIndividual(user);
        relatorio.put("relatorioIndividual", relatorioIndividual);
        
        return relatorio;
    }

    /**
     * Gera relatório individual para PESSOA_FISICA
     */
    private Map<String, Object> gerarRelatorioIndividual(User user) {
        Map<String, Object> individual = new HashMap<>();
        
        // CORRIGIDO: usando String para o ID (MongoDB)
        List<Relato> meusRelatos = relatoRepository.findByUsuarioIdOrderByDataRelatoDesc(user.getId());
        
        individual.put("totalRelatos", meusRelatos.size());
        
        // Relatos por tipo
        Map<String, Long> porTipo = meusRelatos.stream()
                .collect(Collectors.groupingBy(
                    Relato::getTipo,
                    Collectors.counting()
                ));
        individual.put("relatosPorTipo", porTipo);
        
        // Últimos 5 relatos
        List<Map<String, Object>> ultimosRelatos = meusRelatos.stream()
                .limit(5)
                .map(this::relatoToMap)
                .collect(Collectors.toList());
        individual.put("ultimosRelatos", ultimosRelatos);
        
        // Relatos por mês (últimos 6 meses)
        Map<String, Long> porMes = getRelatosPorMes(meusRelatos);
        individual.put("relatosPorMes", porMes);
        
        // Relatos por município
        Map<String, Long> porMunicipio = meusRelatos.stream()
                .filter(r -> r.getMunicipio() != null)
                .collect(Collectors.groupingBy(
                    Relato::getMunicipio,
                    Collectors.counting()
                ));
        individual.put("relatosPorMunicipio", porMunicipio);
        
        // Relatos por bairro (top 10)
        Map<String, Long> porBairro = meusRelatos.stream()
                .filter(r -> r.getBairro() != null)
                .collect(Collectors.groupingBy(
                    Relato::getBairro,
                    Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new
                ));
        individual.put("topBairros", porBairro);
        
        // Relatos por linha de ônibus
        Map<String, Long> porLinha = new HashMap<>();
        meusRelatos.forEach(r -> {
            if (r.getLinhaTranscol() != null) {
                porLinha.merge("Transcol " + r.getLinhaTranscol(), 1L, Math::addExact);
            }
            if (r.getLinhaMunicipal() != null) {
                porLinha.merge("Municipal " + r.getLinhaMunicipal(), 1L, Math::addExact);
            }
        });
        individual.put("relatosPorLinha", porLinha);
        
        // Estatísticas
        Map<String, Object> estatisticas = new HashMap<>();
        estatisticas.put("mediaDiaria", calcularMediaDiaria(meusRelatos));
        estatisticas.put("diaMaisAtivo", getDiaMaisAtivo(meusRelatos));
        estatisticas.put("tipoMaisReportado", getTipoMaisFrequente(porTipo));
        estatisticas.put("municipioMaisReportado", getMunicipioMaisFrequente(porMunicipio));
        individual.put("estatisticas", estatisticas);
        
        return individual;
    }

    /**
     * Gera relatório empresarial consolidado (todos os vinculados)
     */
    private Map<String, Object> gerarRelatorioEmpresarial(User empresa) {
        Map<String, Object> empresarial = new HashMap<>();
        
        // CORRIGIDO: usando o novo nome do método
        List<User> vinculados = userRepository.listarPorCnpjAtivos(empresa.getCnpj());
        
        // Coleta IDs de todos os usuários (empresa + vinculados)
        List<String> todosIds = new ArrayList<>();
        todosIds.add(empresa.getId());
        vinculados.forEach(v -> todosIds.add(v.getId()));
        
        // CORRIGIDO: usando String para IDs (MongoDB)
        List<Relato> todosRelatos = relatoRepository.findByUsuarioIdInOrderByDataRelatoDesc(todosIds);
        
        empresarial.put("totalUsuarios", vinculados.size() + 1);
        empresarial.put("totalRelatos", todosRelatos.size());
        
        // Relatos por usuário
        Map<String, Long> relatosPorUsuario = new LinkedHashMap<>();
        relatosPorUsuario.put(empresa.getNome(), 
            relatoRepository.countByUsuarioId(empresa.getId()));
        
        vinculados.forEach(v -> {
            relatosPorUsuario.put(v.getNome(), 
                relatoRepository.countByUsuarioId(v.getId()));
        });
        empresarial.put("relatosPorUsuario", relatosPorUsuario);
        
        // Relatos por tipo (consolidado)
        Map<String, Long> porTipo = todosRelatos.stream()
                .collect(Collectors.groupingBy(
                    Relato::getTipo,
                    Collectors.counting()
                ));
        empresarial.put("relatosPorTipo", porTipo);
        
        // Relatos por mês (últimos 6 meses)
        Map<String, Long> porMes = getRelatosPorMes(todosRelatos);
        empresarial.put("relatosPorMes", porMes);
        
        // Relatos por município
        Map<String, Long> porMunicipio = todosRelatos.stream()
                .filter(r -> r.getMunicipio() != null)
                .collect(Collectors.groupingBy(
                    Relato::getMunicipio,
                    Collectors.counting()
                ));
        empresarial.put("relatosPorMunicipio", porMunicipio);
        
        // Top 5 usuários mais ativos
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
        
        // Distribuição geográfica (municípios)
        List<Map<String, Object>> distribuicaoGeografica = porMunicipio.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("municipio", entry.getKey());
                    map.put("total", entry.getValue());
                    return map;
                })
                .collect(Collectors.toList());
        empresarial.put("distribuicaoGeografica", distribuicaoGeografica);
        
        // Tendências (comparação mês atual vs anterior)
        Map<String, Object> tendencias = calcularTendencias(todosRelatos);
        empresarial.put("tendencias", tendencias);
        
        return empresarial;
    }

    /**
     * Converte Relato para Map (formato JSON)
     */
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

    /**
     * Agrupa relatos por mês (últimos 6 meses)
     */
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

    /**
     * Calcula média diária de relatos
     */
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

    /**
     * Retorna o dia com mais relatos
     */
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

    /**
     * Retorna o tipo de relato mais frequente
     */
    private String getTipoMaisFrequente(Map<String, Long> porTipo) {
        if (porTipo.isEmpty()) return "N/A";
        
        return porTipo.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " ocorrências)")
                .orElse("N/A");
    }

    /**
     * Retorna o município com mais relatos
     */
    private String getMunicipioMaisFrequente(Map<String, Long> porMunicipio) {
        if (porMunicipio.isEmpty()) return "N/A";
        
        return porMunicipio.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
    }

    /**
     * Calcula tendências comparando mês atual com mês anterior
     */
    private Map<String, Object> calcularTendencias(List<Relato> relatos) {
        Map<String, Object> tendencias = new HashMap<>();
        
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioMesAtual = agora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime inicioMesAnterior = inicioMesAtual.minusMonths(1);
        
        long totalMesAtual = relatos.stream()
                .filter(r -> r.getDataRelato() != null && r.getDataRelato().isAfter(inicioMesAtual))
                .count();
                
        long totalMesAnterior = relatos.stream()
                .filter(r -> r.getDataRelato() != null && 
                         r.getDataRelato().isAfter(inicioMesAnterior) && 
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
}