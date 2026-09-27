package br.com.fundacaomariamae.integracap_hikcentral.dto;

import java.time.LocalDateTime;

public class PassagemDTO {

    private Long id;
    private String codigoAssistido;
    private String nomeAssistido;
    private LocalDateTime dataHora;
    private String terminal;
    private String resultado;

    public PassagemDTO() {
    }

    public PassagemDTO(
            Long id,
            String codigoAssistido,
            String nomeAssistido,
            LocalDateTime dataHora,
            String terminal,
            String resultado) {

        this.id = id;
        this.codigoAssistido = codigoAssistido;
        this.nomeAssistido = nomeAssistido;
        this.dataHora = dataHora;
        this.terminal = terminal;
        this.resultado = resultado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoAssistido() {
        return codigoAssistido;
    }

    public void setCodigoAssistido(String codigoAssistido) {
        this.codigoAssistido = codigoAssistido;
    }

    public String getNomeAssistido() {
        return nomeAssistido;
    }

    public void setNomeAssistido(String nomeAssistido) {
        this.nomeAssistido = nomeAssistido;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}