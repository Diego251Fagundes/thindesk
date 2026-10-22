package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.dto.AberturaChamadoRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import com.pfc.thindesk.repository.ChamadoRepository;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    public Chamado criarChamado(AberturaChamadoRequest dados, String usuario) {
        if (dados.titulo() == null || dados.titulo().isBlank()
                || dados.descricao() == null || dados.descricao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Titulo e descricao sao obrigatorios.");
        }

        Chamado chamado = new Chamado();
        chamado.setTitulo(dados.titulo().strip());
        chamado.setDescricao(dados.descricao().strip());
        chamado.setUsuario(usuario);
        chamado.setDataAbertura(Instant.now());
        chamado.setStatus("ABERTO");
        return chamadoRepository.save(chamado);
    }

    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    public Chamado atualizarChamado(String id, Chamado chamadoAtualizado) {
        return chamadoRepository.findById(id)
                .map(chamado -> {
                    chamado.setDescricao(chamadoAtualizado.getDescricao());
                    chamado.setStatus(chamadoAtualizado.getStatus());
                    chamado.setTipo(chamadoAtualizado.getTipo());
                    chamado.setTecnico(chamadoAtualizado.getTecnico());
                    chamado.setUsuario(chamadoAtualizado.getUsuario());
                    return chamadoRepository.save(chamado);
                })
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado com id: " + id));
    }

}
