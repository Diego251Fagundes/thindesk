package com.pfc.thindesk.controller;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.dto.AberturaChamadoRequest;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import com.pfc.thindesk.service.ChamadoService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @PostMapping
    public ResponseEntity<Chamado> criarChamado(@RequestBody AberturaChamadoRequest dados, Principal solicitante) {
        Chamado chamado = chamadoService.criarChamado(dados, solicitante.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(chamado);
    }

    @GetMapping
    public List<Chamado> listarChamados(Principal solicitante) {
        return chamadoService.listarChamados(solicitante.getName());
    }

    @GetMapping("/usuario/{id}")
    public List<Chamado> listarChamadosDoUsuario(@PathVariable("id") String id, Principal solicitante) {
        return chamadoService.listarChamadosDoUsuario(id, solicitante.getName());
    }

    @GetMapping("/{id}")
    public Chamado consultarChamado(@PathVariable("id") String id, Principal solicitante) {
        return chamadoService.consultarChamado(id, solicitante.getName());
    }

    @PutMapping("/{id}/resolver")
    public Chamado resolverChamado(@PathVariable("id") String id, @RequestBody ResolucaoChamadoRequest dados,
                                  Principal tecnico) {
        return chamadoService.resolverChamado(id, dados, tecnico.getName());
    }

}
