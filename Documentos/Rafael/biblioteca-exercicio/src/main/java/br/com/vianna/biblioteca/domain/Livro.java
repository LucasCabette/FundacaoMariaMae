package br.com.vianna.biblioteca.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Livro {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String titulo;
    @NotBlank private String autor;
    @Column(nullable = false) private boolean disponivel = true;

    protected Livro() { }
    public Livro(String titulo, String autor) { this.titulo = titulo; this.autor = autor; }
    public void emprestar() {
        if (!disponivel) throw new RegraDeNegocioException("O livro não está disponível para empréstimo.");
        disponivel = false;
    }
    public void devolver() { disponivel = true; }
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public boolean isDisponivel() { return disponivel; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
}
