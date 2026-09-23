package br.com.vianna.biblioteca.controller;
import br.com.vianna.biblioteca.domain.RegraDeNegocioException;
import br.com.vianna.biblioteca.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller @RequestMapping("/emprestimos")
public class EmprestimoController {
    private final EmprestimoService emprestimoService; private final LivroService livroService; private final AlunoService alunoService;
    public EmprestimoController(EmprestimoService emprestimoService, LivroService livroService, AlunoService alunoService) { this.emprestimoService = emprestimoService; this.livroService = livroService; this.alunoService = alunoService; }
    @GetMapping public String listar(Model model) { preencher(model); return "emprestimos"; }
    @PostMapping public String realizar(@RequestParam Long livroId, @RequestParam Long alunoId, Model model) {
        try { emprestimoService.realizar(livroId, alunoId); return "redirect:/emprestimos?criado"; }
        catch (RegraDeNegocioException e) { preencher(model); model.addAttribute("erro", e.getMessage()); return "emprestimos"; }
    }
    @PostMapping("/{id}/devolucao") public String devolver(@PathVariable Long id) { emprestimoService.devolver(id); return "redirect:/emprestimos?devolvido"; }
    private void preencher(Model model) { model.addAttribute("emprestimos", emprestimoService.listar()); model.addAttribute("livros", livroService.listar()); model.addAttribute("alunos", alunoService.listar()); }
}
