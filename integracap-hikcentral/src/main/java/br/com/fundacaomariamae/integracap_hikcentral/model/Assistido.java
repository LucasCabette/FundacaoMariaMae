package br.com.fundacaomariamae.integracap_hikcentral.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import br.com.fundacaomariamae.integracap_hikcentral.model.converter.DataSqliteConverter;
import br.com.fundacaomariamae.integracap_hikcentral.model.converter.DataHoraSqliteConverter;

@Entity
@Table(name = "assistido")
public class Assistido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_assistido", nullable = false, unique = true)
    private String codigoAssistido;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    @Convert(converter = DataSqliteConverter.class)
    private LocalDate dataNascimento;

    @Column(name = "nome_mae")
    private String nomeMae;

    @Column(name = "nome_pai")
    private String nomePai;

    @Column(unique = true, length = 11)
    private String cpf;

    private String rg;

    @Column(name = "cidade_nascimento")
    private String cidadeNascimento;

    @Enumerated(EnumType.STRING)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAssistido status = StatusAssistido.ATIVO;

    @Column(name = "data_cadastro", insertable = false, updatable = false)
    @Convert(converter = DataHoraSqliteConverter.class)
    private LocalDateTime dataCadastro;

    @Column(name = "data_atualizacao", insertable = false, updatable = false)
    @Convert(converter = DataHoraSqliteConverter.class)
    private LocalDateTime dataAtualizacao;

    public Long getId() { return id; }
    public String getCodigoAssistido() { return codigoAssistido; }
    public void setCodigoAssistido(String codigoAssistido) { this.codigoAssistido = codigoAssistido; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getNomeMae() { return nomeMae; }
    public void setNomeMae(String nomeMae) { this.nomeMae = nomeMae; }
    public String getNomePai() { return nomePai; }
    public void setNomePai(String nomePai) { this.nomePai = nomePai; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }
    public String getCidadeNascimento() { return cidadeNascimento; }
    public void setCidadeNascimento(String cidadeNascimento) { this.cidadeNascimento = cidadeNascimento; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public StatusAssistido getStatus() { return status; }
    public void setStatus(StatusAssistido status) { this.status = status; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
}
