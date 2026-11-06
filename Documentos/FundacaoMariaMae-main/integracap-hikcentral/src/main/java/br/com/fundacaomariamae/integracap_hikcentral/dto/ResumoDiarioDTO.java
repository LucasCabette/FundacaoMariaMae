package br.com.fundacaomariamae.integracap_hikcentral.dto;

import java.time.LocalDate;

public class ResumoDiarioDTO {

    private LocalDate data;
    private int totalPassagens;
    private int passagensAutorizadas;
    private int passagensRecusadas;
    private int passagensInconclusivas;

    public ResumoDiarioDTO() {
    }

    public ResumoDiarioDTO(
            LocalDate data,
            int totalPassagens,
            int passagensAutorizadas,
            int passagensRecusadas,
            int passagensInconclusivas) {

        this.data = data;
        this.totalPassagens = totalPassagens;
        this.passagensAutorizadas = passagensAutorizadas;
        this.passagensRecusadas = passagensRecusadas;
        this.passagensInconclusivas = passagensInconclusivas;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public int getTotalPassagens() {
        return totalPassagens;
    }

    public void setTotalPassagens(int totalPassagens) {
        this.totalPassagens = totalPassagens;
    }

    public int getPassagensAutorizadas() {
        return passagensAutorizadas;
    }

    public void setPassagensAutorizadas(int passagensAutorizadas) {
        this.passagensAutorizadas = passagensAutorizadas;
    }

    public int getPassagensRecusadas() {
        return passagensRecusadas;
    }

    public void setPassagensRecusadas(int passagensRecusadas) {
        this.passagensRecusadas = passagensRecusadas;
    }

    public int getPassagensInconclusivas() {
        return passagensInconclusivas;
    }

    public void setPassagensInconclusivas(int passagensInconclusivas) {
        this.passagensInconclusivas = passagensInconclusivas;
    }
}