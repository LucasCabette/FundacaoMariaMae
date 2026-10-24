package br.com.fundacaomariamae.integracap_hikcentral.service;

/** Permite ao cliente consultar no historico a tentativa que falhou. */
public class PreparacaoFalhouException extends RuntimeException {
    private final String preparacaoId;
    private final int status;

    public PreparacaoFalhouException(String preparacaoId, int status, String mensagem, RuntimeException causa) {
        super(mensagem, causa);
        this.preparacaoId = preparacaoId;
        this.status = status;
    }

    public String getPreparacaoId() { return preparacaoId; }
    public int getStatus() { return status; }
}
