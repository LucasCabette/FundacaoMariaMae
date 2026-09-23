package br.com.vianna.biblioteca.service;

import br.com.vianna.biblioteca.domain.*;
import br.com.vianna.biblioteca.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class EmprestimoService {
    public static final int LIMITE_EMPRESTIMOS_ATIVOS = 3;
    private final LivroRepository livroRepository;
    private final AlunoRepository alunoRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final AlertaEmprestimoService alertaService;

    public EmprestimoService(LivroRepository livroRepository, AlunoRepository alunoRepository,
                             EmprestimoRepository emprestimoRepository, AlertaEmprestimoService alertaService) {
        this.livroRepository = livroRepository; this.alunoRepository = alunoRepository;
        this.emprestimoRepository = emprestimoRepository; this.alertaService = alertaService;
    }
    @Transactional
    public Emprestimo realizar(Long livroId, Long alunoId) {
        Livro livro = livroRepository.findById(livroId).orElseThrow(() -> new RegraDeNegocioException("Livro não encontrado."));
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(() -> new RegraDeNegocioException("Aluno não encontrado."));
        if (emprestimoRepository.countByAlunoIdAndDataDevolucaoIsNull(alunoId) >= LIMITE_EMPRESTIMOS_ATIVOS)
            throw new RegraDeNegocioException("O aluno já possui o limite de empréstimos ativos.");
        livro.emprestar();
        Emprestimo emprestimo = emprestimoRepository.save(new Emprestimo(livro, aluno));
        alertaService.enviar(emprestimo);
        return emprestimo;
    }
    @Transactional
    public void devolver(Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RegraDeNegocioException("Empréstimo não encontrado."));
        emprestimo.devolver();
        emprestimo.getLivro().devolver();
    }
    public List<Emprestimo> listar() { return emprestimoRepository.findAllByOrderByDataEmprestimoDesc(); }
}
