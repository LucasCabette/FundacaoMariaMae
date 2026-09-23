package br.com.vianna.biblioteca.controller;
import br.com.vianna.biblioteca.domain.Aluno;
import br.com.vianna.biblioteca.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/alunos")
public class AlunoController {
    private final AlunoService service;
    public AlunoController(AlunoService service) { this.service = service; }
    @GetMapping public String listar(Model model) { model.addAttribute("alunos", service.listar()); model.addAttribute("aluno", new Aluno("", "")); return "alunos"; }
    @PostMapping public String cadastrar(@Valid @ModelAttribute Aluno aluno, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("alunos", service.listar()); return "alunos"; }
        service.cadastrar(aluno); return "redirect:/alunos?criado";
    }
}
