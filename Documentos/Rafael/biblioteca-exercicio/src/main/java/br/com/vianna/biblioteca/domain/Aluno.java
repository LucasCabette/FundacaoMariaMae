package br.com.vianna.biblioteca.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Aluno {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank private String nome;
    @NotBlank @Column(unique = true) private String matricula;

    protected Aluno() { }
    public Aluno(String nome, String matricula) { this.nome = nome; this.matricula = matricula; }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getMatricula() { return matricula; }
    public void setNome(String nome) { this.nome = nome; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
}
