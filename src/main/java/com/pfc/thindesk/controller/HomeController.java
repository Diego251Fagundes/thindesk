package com.pfc.thindesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.HorarioAtendimento;
import com.pfc.thindesk.dto.AberturaChamadoRequest;
import com.pfc.thindesk.dto.ResolucaoChamadoRequest;
import com.pfc.thindesk.service.ClienteService;
import com.pfc.thindesk.service.ChamadoService;
import com.pfc.thindesk.service.HorarioAtendimentoService;
import java.util.List;
import java.time.Instant;
import com.pfc.thindesk.service.DashboardService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private ChamadoService chamadoService;
    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private HorarioAtendimentoService horarioAtendimentoService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public ModelAndView home(Authentication autenticacao, @RequestParam(name = "dias", defaultValue = "30") int dias) {
        ModelAndView modelAndView = new ModelAndView("layout");
        boolean tecnico = ehTecnico(autenticacao);
        List<Chamado> chamados = tecnico
                ? chamadoService.listarChamadosParaAtendimento()
                : chamadoService.listarChamados(autenticacao.getName());
        modelAndView.addObject("tecnico", tecnico);
        modelAndView.addObject("dashboard", dashboardService.resumir(chamados, dias, Instant.now()));
        return modelAndView;
    }

    @GetMapping("/chamados")
    public String chamados(Model model, Authentication autenticacao) {
        boolean tecnico = ehTecnico(autenticacao);
        List<Chamado> chamados = tecnico
                ? chamadoService.listarChamadosParaAtendimento()
                : chamadoService.listarChamados(autenticacao.getName());
        model.addAttribute("chamados", chamados);
        model.addAttribute("tecnico", tecnico);
        return "chamados";
    }

    @GetMapping("/chamados/novo")
    public String novoChamado(Model model) {
        model.addAttribute("dados", new AberturaChamadoRequest("", ""));
        return "chamados/novo";
    }

    @PostMapping("/chamados")
    public String abrirChamado(@ModelAttribute("dados") AberturaChamadoRequest dados,
                              Principal solicitante, Model model, RedirectAttributes redirectAttributes) {
        try {
            chamadoService.criarChamado(dados, solicitante.getName());
        } catch (ResponseStatusException erro) {
            if (!erro.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                throw erro;
            }
            model.addAttribute("erro", "Preencha o título e a descrição do chamado.");
            return "chamados/novo";
        }
        redirectAttributes.addFlashAttribute("mensagem", "Chamado aberto com sucesso.");
        return "redirect:/chamados";
    }

    @GetMapping("/chamados/{id}")
    public String detalhesChamado(@PathVariable("id") String id, Model model, Authentication autenticacao) {
        boolean tecnico = ehTecnico(autenticacao);
        Chamado chamado = tecnico
                ? chamadoService.consultarChamadoParaAtendimento(id)
                : chamadoService.consultarChamado(id, autenticacao.getName());
        model.addAttribute("chamado", chamado);
        model.addAttribute("tecnico", tecnico);
        if (!model.containsAttribute("dados")) {
            model.addAttribute("dados", new ResolucaoChamadoRequest(""));
        }
        return "chamados/detalhes";
    }

    @PostMapping("/chamados/{id}/resolver")
    public String resolverChamado(@PathVariable("id") String id,
                                 @ModelAttribute("dados") ResolucaoChamadoRequest dados,
                                 Authentication autenticacao, Model model, RedirectAttributes redirectAttributes) {
        try {
            chamadoService.resolverChamado(id, dados, autenticacao.getName());
        } catch (ResponseStatusException erro) {
            if (erro.getStatusCode().equals(HttpStatus.BAD_REQUEST)) {
                model.addAttribute("erro", "Informe a solução antes de resolver o chamado.");
            } else if (erro.getStatusCode().equals(HttpStatus.CONFLICT)) {
                model.addAttribute("erro", "Este chamado já foi resolvido e não pode ser alterado.");
            } else {
                throw erro;
            }
            return detalhesChamado(id, model, autenticacao);
        }
        redirectAttributes.addFlashAttribute("mensagem", "Chamado resolvido com sucesso.");
        return "redirect:/chamados/" + id;
    }

    private boolean ehTecnico(Authentication autenticacao) {
        return autenticacao.getAuthorities().stream()
                .anyMatch(permissao -> permissao.getAuthority().equals("ROLE_TECNICO"));
    }

    @GetMapping("/clientes")
    public String clientes(Model model) {
        List<Cliente> clientes = clienteService.listarClientes();
        model.addAttribute("clientes", clientes);
        String fragment = "clientes :: content";
        log.info("Carregando fragmento: {}", fragment); // Log para depuração
        model.addAttribute("content", fragment);
        return "clientes";
    }
    
    @GetMapping("/ajustes-horarios")
    public String horarios(Model model) {
        List<HorarioAtendimento> horarios = horarioAtendimentoService.listarTodos();
        model.addAttribute("ajustes-horarios", horarios);
        String fragment = "ajustes-horarios :: content";
        log.info("Carregando fragmento: {}", fragment); // Log para depuração
        model.addAttribute("content", fragment);
        return "ajustes-horarios";
    }

}
