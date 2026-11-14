package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DashboardService {
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    public Resumo resumir(List<Chamado> chamados, int dias, Instant agora) {
        if (dias != 30 && dias != 60 && dias != 90) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione 30, 60 ou 90 dias.");
        }
        LocalDate fim = agora.atZone(FUSO).toLocalDate();
        LocalDate inicio = fim.minusDays(dias - 1);
        Instant limite = inicio.atStartOfDay(FUSO).toInstant();
        List<Chamado> periodo = chamados.stream()
                .filter(c -> c.getDataAbertura() != null && !c.getDataAbertura().isBefore(limite)
                        && !c.getDataAbertura().isAfter(agora))
                .sorted(Comparator.comparing(Chamado::getDataAbertura).reversed()).toList();
        long abertos = periodo.stream().filter(c -> "ABERTO".equals(c.getStatus())).count();
        long resolvidos = periodo.stream().filter(c -> "RESOLVIDO".equals(c.getStatus())).count();
        Map<LocalDate, long[]> movimento = new TreeMap<>();
        inicio.datesUntil(fim.plusDays(1)).forEach(d -> movimento.put(d, new long[2]));
        Map<String, List<Long>> temposTecnicos = new TreeMap<>();
        List<Long> tempos = new ArrayList<>();
        for (Chamado chamado : periodo) {
            movimento.get(chamado.getDataAbertura().atZone(FUSO).toLocalDate())[0]++;
            if ("RESOLVIDO".equals(chamado.getStatus()) && chamado.getDataResolucao() != null
                    && !chamado.getDataResolucao().isBefore(chamado.getDataAbertura())
                    && !chamado.getDataResolucao().isAfter(agora)) {
                movimento.get(chamado.getDataResolucao().atZone(FUSO).toLocalDate())[1]++;
                long segundos = Duration.between(chamado.getDataAbertura(), chamado.getDataResolucao()).getSeconds();
                tempos.add(segundos);
                if (chamado.getTecnico() != null && !chamado.getTecnico().isBlank()) {
                    temposTecnicos.computeIfAbsent(chamado.getTecnico(), nome -> new ArrayList<>()).add(segundos);
                }
            }
        }
        List<Tecnico> rankingCompleto = temposTecnicos.entrySet().stream()
                .map(item -> new Tecnico(item.getKey(), item.getValue().size(), media(item.getValue())))
                .sorted(Comparator.comparingDouble(Tecnico::mediaSegundos)
                        .thenComparing(Comparator.comparingLong(Tecnico::resolvidos).reversed())
                        .thenComparing(Tecnico::nome)).limit(5).toList();
        double maiorTempo = rankingCompleto.stream().mapToDouble(Tecnico::mediaSegundos).max().orElse(0);
        List<LinhaRanking> ranking = rankingCompleto.stream()
                .map(t -> new LinhaRanking(t.nome(), t.resolvidos(), formatarTempo(t.mediaSegundos()),
                        maiorTempo == 0 ? 0 : t.mediaSegundos() / maiorTempo * 100)).toList();
        List<Dia> evolucao = movimento.entrySet().stream()
                .map(item -> new Dia(item.getKey(), item.getValue()[0], item.getValue()[1])).toList();
        int total = periodo.size();
        double percentualResolvidos = total == 0 ? 0 : resolvidos * 100.0 / total;
        return new Resumo(dias, inicio, fim, total, abertos, resolvidos, total - abertos - resolvidos,
                percentualResolvidos, total == 0 ? 0 : abertos * 100.0 / total,
                Math.round(percentualResolvidos), tempos.isEmpty() ? "—" : formatarTempo(media(tempos)),
                tempos.size(), chamados.stream().filter(c -> c.getDataAbertura() == null).count(),
                periodo.stream().limit(5).toList(), evolucao, ranking);
    }

    private static double media(List<Long> tempos) {
        return tempos.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private static String formatarTempo(double segundos) {
        long minutos = (long) (segundos / 60);
        if (minutos == 0) return "< 1 min";
        if (minutos < 60) return minutos + " min";
        long horas = minutos / 60;
        if (horas < 24) return horas + " h" + (minutos % 60 == 0 ? "" : " " + minutos % 60 + " min");
        return horas / 24 + " d" + (horas % 24 == 0 ? "" : " " + horas % 24 + " h");
    }

    private record Tecnico(String nome, long resolvidos, double mediaSegundos) {}
    public record LinhaRanking(String nome, long resolvidos, String tempoMedio, double percentualTempo) {}
    public record Dia(LocalDate data, long aberturas, long resolucoes) {}
    public record Resumo(int dias, LocalDate inicio, LocalDate fim, long total, long abertos, long resolvidos,
                         long outros, double percentualResolvidos, double percentualAbertos, long taxaResolucao,
                         String tempoMedio, long amostrasTempo, long semData,
                         List<Chamado> recentes, List<Dia> evolucao, List<LinhaRanking> ranking) {}
}
