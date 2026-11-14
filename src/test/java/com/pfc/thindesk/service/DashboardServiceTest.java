package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {
    private final DashboardService service = new DashboardService();
    private final Instant agora = Instant.parse("2026-11-14T20:00:00Z");

    private Chamado chamado(String abertura, String resolucao, String tecnico) {
        Chamado c = new Chamado();
        c.setDataAbertura(abertura == null ? null : Instant.parse(abertura));
        c.setDataResolucao(resolucao == null ? null : Instant.parse(resolucao));
        c.setStatus(resolucao == null ? "ABERTO" : "RESOLVIDO");
        c.setTecnico(tecnico);
        return c;
    }

    @Test
    void filtraDiasCalendarioNoFusoLocalIncluindoOsLimites() {
        List<Chamado> dados = List.of(
                chamado("2026-10-16T02:59:59Z", null, null),
                chamado("2026-10-16T03:00:00Z", null, null),
                chamado("2026-11-14T20:00:00Z", null, null),
                chamado("2026-11-14T20:00:01Z", null, null),
                chamado(null, null, null));
        var resumo = service.resumir(dados, 30, agora);
        assertEquals(LocalDate.of(2026, 10, 16), resumo.inicio());
        assertEquals(2, resumo.total());
        assertEquals(1, resumo.semData());
        assertEquals(30, resumo.evolucao().size());
        assertEquals(1, resumo.evolucao().getFirst().aberturas());
        assertEquals(1, resumo.evolucao().getLast().aberturas());
    }

    @Test
    void ampliaFiltroParaSessentaENoventaDias() {
        var dados = List.of(chamado("2026-10-01T12:00:00Z", null, null),
                chamado("2026-09-01T12:00:00Z", null, null));
        assertEquals(0, service.resumir(dados, 30, agora).total());
        assertEquals(1, service.resumir(dados, 60, agora).total());
        assertEquals(2, service.resumir(dados, 90, agora).total());
        assertThrows(ResponseStatusException.class, () -> service.resumir(dados, 45, agora));
    }

    @Test
    void ordenaRankingPorMediaNaoPorQuantidadeOuResolucaoMaisRapidaIsolada() {
        var resumo = service.resumir(List.of(
                chamado("2026-11-10T12:00:00Z", "2026-11-10T13:00:00Z", "ana"),
                chamado("2026-11-11T12:00:00Z", "2026-11-11T15:00:00Z", "ana"),
                chamado("2026-11-12T12:00:00Z", "2026-11-12T13:30:00Z", "bruno")), 30, agora);
        assertEquals("bruno", resumo.ranking().getFirst().nome());
        assertEquals("1 h 30 min", resumo.ranking().getFirst().tempoMedio());
        assertEquals(2, resumo.ranking().getLast().resolvidos());
        assertEquals("2 h", resumo.ranking().getLast().tempoMedio());
        assertEquals("1 h 50 min", resumo.tempoMedio());
        assertEquals(3, resumo.amostrasTempo());
        assertEquals(100, resumo.taxaResolucao());
    }

    @Test
    void ignoraDuracoesInvalidasSemEsconderChamadosDosTotais() {
        var resumo = service.resumir(List.of(
                chamado("2026-11-10T12:00:00Z", "2026-11-10T11:00:00Z", "ana"),
                chamado("2026-11-10T12:00:00Z", "2026-11-15T12:00:00Z", "ana"),
                chamado("2026-11-10T12:00:00Z", null, null)), 30, agora);
        assertEquals(3, resumo.total());
        assertEquals(2, resumo.resolvidos());
        assertEquals(0, resumo.amostrasTempo());
        assertEquals("—", resumo.tempoMedio());
        assertTrue(resumo.ranking().isEmpty());
        assertEquals(0, resumo.evolucao().stream().mapToLong(DashboardService.Dia::resolucoes).sum());
    }

    @Test
    void vazioNaoProduzPercentuaisIndefinidosEPreencheSerieComZeros() {
        var resumo = service.resumir(List.of(), 90, agora);
        assertEquals(0, resumo.percentualResolvidos());
        assertEquals(0, resumo.percentualAbertos());
        assertEquals(90, resumo.evolucao().size());
        assertTrue(resumo.recentes().isEmpty());
        assertEquals(0, resumo.evolucao().stream().mapToLong(DashboardService.Dia::aberturas).sum());
    }

    @Test
    void usaAsDatasDeCadaEventoNoGrafico() {
        var resumo = service.resumir(List.of(chamado("2026-11-10T12:00:00Z", "2026-11-12T13:00:00Z", "ana")), 30, agora);
        var abertura = resumo.evolucao().stream().filter(d -> d.data().equals(LocalDate.of(2026, 11, 10))).findFirst().orElseThrow();
        var resolucao = resumo.evolucao().stream().filter(d -> d.data().equals(LocalDate.of(2026, 11, 12))).findFirst().orElseThrow();
        assertEquals(1, abertura.aberturas());
        assertEquals(0, abertura.resolucoes());
        assertEquals(1, resolucao.resolucoes());
    }
}
