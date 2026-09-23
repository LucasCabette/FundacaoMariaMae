package br.com.vianna.biblioteca.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Emprestimo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Livro livro;
    @ManyToOne(optional = false) private Aluno aluno;
    @Column(nullable = false) private LocalDate dataEmprestimo;
    private LocalDate dataDevolucao;

    protected Emprestimo() { }
    public Emprestimo(Livro livro, Aluno aluno) {
        this.livro = livro; this.aluno = aluno; this.dataEmprestimo = LocalDate.now();
    }
    public void devolver() {
        if (dataDevolucao != null) throw new RegraDeNegocioException("Este empréstimo já foi devolvido.");
        dataDevolucao = LocalDate.now();
    }
    public Long getId() { return id; }
    public Livro getLivro() { return livro; }
    public Aluno getAluno() { return aluno; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataDevolucao() { return dataDevolucao; }
    public boolean isAtivo() { return dataDevolucao == null; }
}
