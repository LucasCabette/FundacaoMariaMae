package br.com.vianna.biblioteca.controller;
import br.com.vianna.biblioteca.domain.Livro;
import br.com.vianna.biblioteca.service.LivroService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/livros")
public class LivroController {
    private final LivroService service;
    public LivroController(LivroService service) { this.service = service; }
    @GetMapping public String listar(Model model) { model.addAttribute("livros", service.listar()); model.addAttribute("livro", new Livro("", "")); return "livros"; }
    @PostMapping public String cadastrar(@Valid @ModelAttribute Livro livro, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("livros", service.listar()); return "livros"; }
        service.cadastrar(livro); return "redirect:/livros?criado";
    }
}
