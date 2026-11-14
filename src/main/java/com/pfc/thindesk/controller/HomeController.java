package com.pfc.thindesk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.pfc.thindesk.entity.Cliente;
import com.pfc.thindesk.entity.Chamado;
import com.pfc.thindesk.entity.HorarioAtendimento;
import com.pfc.thindesk.dto.AberturaChamadoRequest;
import com.pfc.thindesk.service.ClienteService;
import com.pfc.thindesk.service.ChamadoService;
import com.pfc.thindesk.service.HorarioAtendimentoService;
import java.util.List;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    private ClienteService clienteService;
    @Autowired
    private HorarioAtendimentoService horarioAtendimentoService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView modelAndView = new ModelAndView("layout");
        String fragment = "home :: content";
        log.info("Carregando fragmento: {}", fragment); // Log para depuração
        modelAndView.addObject("content", fragment);
        return modelAndView;
    }

    @GetMapping("/chamados")
    public String chamados(Model model, Principal solicitante) {
        List<Chamado> chamados = chamadoService.listarChamados(solicitante.getName());
        model.addAttribute("chamados", chamados);
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
