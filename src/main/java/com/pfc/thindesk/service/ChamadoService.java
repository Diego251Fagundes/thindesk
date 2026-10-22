package com.pfc.thindesk.service;

import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.dto.AberturaChamadoRequest;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import java.util.List;
import com.pfc.thindesk.repository.ChamadoRepository;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

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

    public List<Chamado> listarChamados(String usuario) {
        return chamadoRepository.findByUsuario(usuario);
    }

    public List<Chamado> listarChamadosDoUsuario(String usuario, String solicitante) {
        validarAcesso(usuario, solicitante);
        return listarChamados(usuario);
    }

    public Chamado consultarChamado(String id, String solicitante) {
        Chamado chamado = chamadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado nao encontrado."));
        validarAcesso(chamado.getUsuario(), solicitante);
        return chamado;
    }

    private void validarAcesso(String usuario, String solicitante) {
        if (!solicitante.equals(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Voce pode consultar apenas os seus chamados.");
        }
    }

    public Chamado resolverChamado(String id, ResolucaoChamadoRequest dados, String tecnico) {
        if (dados.solucao() == null || dados.solucao().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A solucao e obrigatoria.");
        }

        if (!chamadoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chamado nao encontrado.");
        }

        // A condicao no MongoDB impede duas resolucoes do mesmo chamado.
        Query consulta = Query.query(Criteria.where("id").is(id).and("status").is("ABERTO"));
        Update atualizacao = new Update()
                .set("solucao", dados.solucao().strip())
                .set("tecnico", tecnico)
                .set("status", "RESOLVIDO")
                .set("dataResolucao", Instant.now());
        Chamado chamado = mongoTemplate.findAndModify(consulta, atualizacao,
                FindAndModifyOptions.options().returnNew(true), Chamado.class);

        if (chamado == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chamados resolvidos nao podem ser alterados.");
        }
        return chamado;
    }

}
