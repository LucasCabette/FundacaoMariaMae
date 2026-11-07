package br.com.fundacaomariamae.integracap_hikcentral.service;

import br.com.fundacaomariamae.integracap_hikcentral.config.HikCentralProperties;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente Artemis/HikCentral. Segredos vêm somente de configuração externa. */
@Component
public class HikCentralOpenApiClient {
    private final HikCentralProperties properties;

    public HikCentralOpenApiClient(HikCentralProperties properties) {
        this.properties = properties;
    }

    public String cadastrarOuAtualizarPessoa(String personCode, Map<String, Object> pessoa) {
        Map<?, ?> consulta = postSemValidarCodigo(
                "/artemis/api/resource/v1/person/personCode/personInfo", Map.of("personCode", personCode));
        if (consulta != null && "0".equals(String.valueOf(consulta.get("code")))
                && consulta.get("data") instanceof Map<?, ?> dados
                && dados.get("personId") != null) {
            Map<String, Object> atualizacao = new java.util.LinkedHashMap<>(pessoa);
            atualizacao.put("personId", dados.get("personId").toString());
            post("/artemis/api/resource/v1/person/single/update", atualizacao);
            return dados.get("personId").toString();
        }
        Map<?, ?> resposta = post("/artemis/api/resource/v1/person/single/add", pessoa);
        Object id = resposta.get("data");
        if (id == null || id.toString().isBlank()) {
            throw new IllegalStateException("HikCentral não retornou o identificador da pessoa");
        }
        return id.toString();
    }

    public void atualizarPessoaExistente(String personCode, Map<String, Object> pessoa) {
        Map<?, ?> consulta = postSemValidarCodigo(
                "/artemis/api/resource/v1/person/personCode/personInfo", Map.of("personCode", personCode));
        if (consulta == null || !"0".equals(String.valueOf(consulta.get("code")))) {
            String detalhe = consulta == null ? "resposta vazia" : String.valueOf(consulta.get("msg"));
            throw new IllegalStateException("Não foi possível localizar o cadastro no HikCentral: " + detalhe);
        }
        if (!(consulta.get("data") instanceof Map<?, ?> dados) || dados.get("personId") == null) {
            throw new IllegalStateException("Cadastro ainda não existe no HikCentral; nenhum registro remoto foi criado.");
        }
        Map<String, Object> atualizacao = new java.util.LinkedHashMap<>(pessoa);
        atualizacao.put("personId", dados.get("personId").toString());
        post("/artemis/api/resource/v1/person/single/update", atualizacao);
    }

    public void atualizarFoto(String personCode, String fotoBase64) {
        post("/artemis/api/resource/v1/person/face/update",
                Map.of("personCode", personCode, "faceData", fotoBase64));
    }

    public void atribuirNivelAcesso(String personId, String privilegeGroupId) {
        post("/artemis/api/acs/v1/privilege/group/single/addPersons", Map.of(
                "privilegeGroupId", privilegeGroupId,
                "type", 1,
                "list", java.util.List.of(Map.of("id", personId))));
    }

    private Map<?, ?> post(String path, Map<String, ?> body) {
        Map<?, ?> resposta = postSemValidarCodigo(path, body);
        if (resposta == null || !"0".equals(String.valueOf(resposta.get("code")))) {
            String detalhe = resposta == null ? "resposta vazia" : String.valueOf(resposta.get("msg"));
            throw new IllegalStateException("OpenAPI HikCentral rejeitou a solicitação: " + detalhe);
        }
        return resposta;
    }

    private Map<?, ?> postSemValidarCodigo(String path, Map<String, ?> body) {
        validarConfiguracao();
        String baseUrl = properties.getBaseUrl().replaceAll("/+$", "");
        String assinatura = assinar(path);
        return RestClient.builder().baseUrl(baseUrl).build()
                .post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("x-ca-key", properties.getApiKey())
                .header("x-ca-signature-headers", "x-ca-key")
                .header("x-ca-signature", assinatura)
                .body(body)
                .retrieve().body(Map.class);
    }

    private String assinar(String path) {
        try {
            String canonical = "POST\napplication/json\napplication/json\nx-ca-key:"
                    + properties.getApiKey() + "\n" + path;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getApiSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception erro) {
            throw new IllegalStateException("Não foi possível assinar a chamada à OpenAPI", erro);
        }
    }

    private void validarConfiguracao() {
        if (properties.getBaseUrl().isBlank() || properties.getApiKey().isBlank()
                || properties.getApiSecret().isBlank()) {
            throw new IllegalStateException("Configure URL, API Key e API Secret do HikCentral");
        }
        if (!properties.getBaseUrl().startsWith("https://")) {
            throw new IllegalStateException("A URL da OpenAPI deve usar HTTPS");
        }
    }
}
