package br.com.fundacaomariamae.integracaphikcentral.dto;

import java.time.LocalDate;
import java.util.List;

public class RelatorioPassagemDTO {

    private LocalDate dataInicio;
    private LocalDate dataFim;
    private List<PassagemDTO> passagens;
    private int totalPassagens;

    public RelatorioPassagemDTO() {
    }

    public RelatorioPassagemDTO(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<PassagemDTO> passagens,
            int totalPassagens) {

        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.passagens = passagens;
        this.totalPassagens = totalPassagens;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public List<PassagemDTO> getPassagens() {
        return passagens;
    }

    public void setPassagens(List<PassagemDTO> passagens) {
        this.passagens = passagens;
    }

    public int getTotalPassagens() {
        return totalPassagens;
    }

    public void setTotalPassagens(int totalPassagens) {
        this.totalPassagens = totalPassagens;
    }
}