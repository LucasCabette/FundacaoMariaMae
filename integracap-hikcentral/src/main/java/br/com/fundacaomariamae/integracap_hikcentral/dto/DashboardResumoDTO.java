package br.com.fundacaomariamae.integracap_hikcentral.dto;

public class DashboardResumoDTO {

    private int totalCadastros;
    private int passagensHoje;
    private int passagensAutorizadas;
    private int passagensRecusadas;
    private int novosCadastros;
    private int erros;

    public DashboardResumoDTO() {
    }

    public DashboardResumoDTO(
            int totalCadastros,
            int passagensHoje,
            int passagensAutorizadas,
            int passagensRecusadas,
            int novosCadastros,
            int erros) {

        this.totalCadastros = totalCadastros;
        this.passagensHoje = passagensHoje;
        this.passagensAutorizadas = passagensAutorizadas;
        this.passagensRecusadas = passagensRecusadas;
        this.novosCadastros = novosCadastros;
        this.erros = erros;
    }

    public int getTotalCadastros() {
        return totalCadastros;
    }

    public void setTotalCadastros(int totalCadastros) {
        this.totalCadastros = totalCadastros;
    }

    public int getPassagensHoje() {
        return passagensHoje;
    }

    public void setPassagensHoje(int passagensHoje) {
        this.passagensHoje = passagensHoje;
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

    public int getNovosCadastros() {
        return novosCadastros;
    }

    public void setNovosCadastros(int novosCadastros) {
        this.novosCadastros = novosCadastros;
    }

    public int getErros() {
        return erros;
    }

    public void setErros(int erros) {
        this.erros = erros;
    }
}