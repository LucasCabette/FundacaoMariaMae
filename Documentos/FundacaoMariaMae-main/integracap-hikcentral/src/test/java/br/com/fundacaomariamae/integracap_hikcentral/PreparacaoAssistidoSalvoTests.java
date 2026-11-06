package br.com.fundacaomariamae.integracap_hikcentral;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import br.com.fundacaomariamae.integracap_hikcentral.service.HistoricoPreparacaoService;
import br.com.fundacaomariamae.integracap_hikcentral.dto.FinalidadePreparacao;

/** Usa banco temporario com pessoas ficticias; nao altera o SQLite do projeto. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PreparacaoAssistidoSalvoTests {
    private static final String BANCO = criarBanco();
    private static final String BOUNDARY = "LucasPasso1Boundary";
    @LocalServerPort private int port;
    @Autowired private JdbcTemplate jdbc;

    private static String criarBanco() {
        try {
            Files.createDirectories(Path.of("target"));
            Path arquivo = Files.createTempDirectory(Path.of("target"), "passo1-").resolve("teste.sqlite");
            String url = "jdbc:sqlite:" + arquivo.toAbsolutePath();
            try (var c = DriverManager.getConnection(url); var s = c.createStatement()) {
                s.execute("""
                    CREATE TABLE assistido (
                        id INTEGER PRIMARY KEY, codigo_assistido TEXT NOT NULL UNIQUE,
                        nome TEXT NOT NULL, data_nascimento TEXT NOT NULL,
                        nome_mae TEXT, nome_pai TEXT, cpf TEXT, rg TEXT,
                        cidade_nascimento TEXT, sexo TEXT, status TEXT NOT NULL,
                        data_cadastro TEXT, data_atualizacao TEXT
                    )
                    """);
                s.execute("""
                    INSERT INTO assistido (id,codigo_assistido,nome,data_nascimento,nome_mae,cpf,status)
                    VALUES (11,'0000000011','Pessoa Ficticia A','1980-05-02','Mae Ficticia A',NULL,'ATIVO'),
                           (12,'0000000012','Pessoa Ficticia B','1992-09-15','Mae Ficticia B','12345678901','ATIVO')
                    """);
            }
            return url;
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }

    @DynamicPropertySource
    static void configurarBanco(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> BANCO);
    }

    private byte[] foto() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 12, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }

    private HttpResponse<String> chamar(String rota, String organizacao, byte[] foto,
                                        String cadastro) throws Exception {
        return chamar(rota, organizacao, foto, cadastro, null);
    }

    private HttpResponse<String> chamar(String rota, String organizacao, byte[] foto,
                                        String cadastro, String finalidade) throws Exception {
        var body = new ByteArrayOutputStream();
        if (finalidade != null) {
            texto(body, "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"finalidade\"\r\n\r\n" + finalidade + "\r\n");
        }
        if (organizacao != null) {
            texto(body, "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"codigoOrganizacao\"\r\n\r\n" + organizacao + "\r\n");
        }
        if (cadastro != null) {
            texto(body, "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"cadastro\"\r\nContent-Type: application/json\r\n\r\n" + cadastro + "\r\n");
        }
        if (foto != null) {
            texto(body, "--" + BOUNDARY + "\r\nContent-Disposition: form-data; name=\"foto\"; filename=\"teste.png\"\r\nContent-Type: image/png\r\n\r\n");
            body.write(foto); texto(body, "\r\n");
        }
        texto(body, "--" + BOUNDARY + "--\r\n");
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/sincronizacao/" + rota))
                .header("Content-Type", "multipart/form-data; boundary=" + BOUNDARY)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray())).build();
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    private static void texto(ByteArrayOutputStream out, String texto) throws Exception {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }

    @Test void usaCadastroDoSQLiteSemExigirCpfENaoAlteraBanco() throws Exception {
        byte[] imagem = foto();
        var response = chamar("assistidos/11/preparar", "ORG-TESTE", imagem,
                "{\"nome\":\"Nome que nao pode substituir o cadastro\"}");
        assertEquals(200, response.statusCode(), response.body());
        var json = new ObjectMapper().readTree(response.body());
        assertEquals(11, json.get("assistidoId").asInt());
        assertEquals("0000000011", json.get("codigoOrigem").asText());
        assertEquals("Pessoa Ficticia A", json.get("nome").asText());
        assertEquals("1980-05-02", json.get("dataNascimento").asText());
        assertEquals("Mae Ficticia A", json.get("nomeMae").asText());
        assertTrue(json.get("cpf").isNull());
        assertEquals("PREPARADO_LOCALMENTE", json.get("estado").asText());
        assertEquals("INTERNO_PROVISORIO", json.get("contrato").asText());
        assertEquals("CADASTRO", json.get("finalidade").asText());
        assertArrayEquals(imagem, Base64.getDecoder().decode(json.get("foto").get("base64").asText()));
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            var rows = s.executeQuery("SELECT COUNT(*) FROM assistido");
            assertTrue(rows.next()); assertEquals(2, rows.getInt(1));
            var row = s.executeQuery("SELECT nome,cpf FROM assistido WHERE id=11");
            assertTrue(row.next()); assertEquals("Pessoa Ficticia A", row.getString(1)); assertNull(row.getString(2));
        }
    }

    @Test void incluiCpfQuandoExistente() throws Exception {
        var response = chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null);
        assertEquals(200, response.statusCode());
        assertEquals("12345678901", new ObjectMapper().readTree(response.body()).get("cpf").asText());
    }

    @Test void listagemLeDatasSQLiteEmTextoENumero() throws Exception {
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            s.executeUpdate("UPDATE assistido SET data_nascimento='640407600000', data_cadastro='2026-10-24 10:20:30', data_atualizacao='2026-10-24 11:22:33' WHERE id=12");
            try (var client = HttpClient.newHttpClient()) {
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/assistidos")).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode(), response.body());
                var json = new ObjectMapper().readTree(response.body());
                assertEquals(2, json.size());
                var pessoa = json.get(0).get("id").asInt() == 12 ? json.get(0) : json.get(1);
                assertEquals("1990-04-18", pessoa.get("dataNascimento").asText());
                assertEquals("2026-10-24T10:20:30", pessoa.get("dataCadastro").asText());
                assertEquals("2026-10-24T11:22:33", pessoa.get("dataAtualizacao").asText());
            } finally {
                s.executeUpdate("UPDATE assistido SET data_nascimento='1992-09-15',data_cadastro=NULL,data_atualizacao=NULL WHERE id=12");
            }
        }
    }

    private HttpResponse<String> json(String metodo, String rota, String corpo) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/assistidos" + rota))
                .header("Content-Type", "application/json")
                .method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo)).build();
        try (var client = HttpClient.newHttpClient()) {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test void cadastroConsultaEdicaoEDuplicidadeContinuamFuncionando() throws Exception {
        String corpo = """
                {"codigoAssistido":"TESTE-DATAS","nome":"Pessoa Ficticia Datas",
                 "dataNascimento":"1987-03-25","nomeMae":"Mae Teste","cpf":null}
                """;
        try {
            var post = json("POST", "", corpo);
            assertEquals(201, post.statusCode(), post.body());
            long id = new ObjectMapper().readTree(post.body()).get("id").asLong();
            var get = json("GET", "/" + id, null);
            assertEquals(200, get.statusCode(), get.body());
            assertEquals("1987-03-25", new ObjectMapper().readTree(get.body()).get("dataNascimento").asText());
            var put = json("PUT", "/" + id, corpo.replace("Mae Teste", "Mae Atualizada"));
            assertEquals(200, put.statusCode(), put.body());
            var depois = json("GET", "/" + id, null);
            var registro = new ObjectMapper().readTree(depois.body());
            assertEquals("1987-03-25", registro.get("dataNascimento").asText());
            assertEquals("Mae Atualizada", registro.get("nomeMae").asText());
            assertEquals(409, json("POST", "", corpo.replace("TESTE-DATAS", "OUTRO-CODIGO")).statusCode());
        } finally {
            try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
                s.executeUpdate("DELETE FROM assistido WHERE codigo_assistido IN ('TESTE-DATAS','OUTRO-CODIGO')");
            }
        }
    }

    @Test void duplicidadeConsideraNascimentoAntigoEmMilissegundos() throws Exception {
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            s.executeUpdate("UPDATE assistido SET data_nascimento='640407600000' WHERE id=12");
            try {
                String corpo = """
                        {"codigoAssistido":"DUPLICADO-LEGADO","nome":"Pessoa Ficticia B",
                         "dataNascimento":"1990-04-18","cpf":null}
                        """;
                var response = json("POST", "", corpo);
                assertEquals(409, response.statusCode(), response.body());
            } finally {
                s.executeUpdate("UPDATE assistido SET data_nascimento='1992-09-15' WHERE id=12");
                s.executeUpdate("DELETE FROM assistido WHERE codigo_assistido='DUPLICADO-LEGADO'");
            }
        }
    }
    @Test void interpretaFormatosSQLiteERejeitaNascimentoInvalido() throws Exception {
        long millis = java.time.LocalDate.of(1992, 9, 15).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli();
        try (var c = DriverManager.getConnection(BANCO);
             var update = c.prepareStatement("UPDATE assistido SET data_nascimento=? WHERE id=12")) {
            try {
                for (String formato : new String[]{"1992-09-15 00:00:00", "1992-09-15T00:00:00", Long.toString(millis)}) {
                    update.setString(1, formato); update.executeUpdate();
                    var response = chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null);
                    assertEquals(200, response.statusCode(), response.body());
                    assertEquals("1992-09-15", new ObjectMapper().readTree(response.body()).get("dataNascimento").asText());
                }
                update.setString(1, "data-invalida"); update.executeUpdate();
                assertEquals(422, chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null).statusCode());
            } finally {
                update.setString(1, "1992-09-15"); update.executeUpdate();
            }
        }
    }
    @Test void cadastroInexistenteRetorna404() throws Exception {
        assertEquals(404, chamar("assistidos/999/preparar", "ORG-TESTE", foto(), null).statusCode());
    }
    @Test void identificadorInvalidoRetorna400() throws Exception {
        assertEquals(400, chamar("assistidos/0/preparar", "ORG-TESTE", foto(), null).statusCode());
    }
    @Test void fotoInvalidaRetorna400() throws Exception {
        assertEquals(400, chamar("assistidos/11/preparar", "ORG-TESTE", new byte[]{1,2,3}, null).statusCode());
    }
    @Test void fotoAusenteRetorna400() throws Exception {
        assertEquals(400, chamar("assistidos/11/preparar", "ORG-TESTE", null, null).statusCode());
    }
    @Test void organizacaoVaziaRetorna400() throws Exception {
        assertEquals(400, chamar("assistidos/11/preparar", " ", foto(), null).statusCode());
    }
    @Test void endpointAnteriorContinuaDisponivel() throws Exception {
        var response = chamar("preparar", null, foto(),
                "{\"codigoOrigem\":\"001\",\"nome\":\"Teste Anterior\",\"codigoOrganizacao\":\"ORG-TESTE\"}");
        assertEquals(200, response.statusCode(), response.body());
    }

    @Test void preparaAtualizacaoDeFotoComCadastroSalvo() throws Exception {
        var response = chamar("assistidos/11/preparar", "ORG-TESTE", foto(), null, "ATUALIZACAO_FOTO");
        assertEquals(200, response.statusCode(), response.body());
        var json = new ObjectMapper().readTree(response.body());
        assertEquals("ATUALIZACAO_FOTO", json.get("finalidade").asText());
        assertEquals("Pessoa Ficticia A", json.get("nome").asText());
        assertEquals("PREPARADO_LOCALMENTE", json.get("estado").asText());
    }

    @Test void rejeitaFinalidadeDesconhecida() throws Exception {
        assertEquals(400, chamar("assistidos/11/preparar", "ORG-TESTE", foto(), null, "ENVIAR_AGORA").statusCode());
    }

    @Test void rejeitaCadastroInativoSemMudarOStatus() throws Exception {
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            s.executeUpdate("UPDATE assistido SET status='INATIVO' WHERE id=12");
            try {
                assertEquals(409, chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null, "CADASTRO").statusCode());
                assertEquals(409, chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null, "ATUALIZACAO_FOTO").statusCode());
                var row = s.executeQuery("SELECT status FROM assistido WHERE id=12");
                assertTrue(row.next()); assertEquals("INATIVO", row.getString(1));
            } finally { s.executeUpdate("UPDATE assistido SET status='ATIVO' WHERE id=12"); }
        }
    }

    @Test void rejeitaNomeEmBrancoDoCadastroSalvo() throws Exception {
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            s.executeUpdate("UPDATE assistido SET nome=' ' WHERE id=12");
            try {
                assertEquals(422, chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null).statusCode());
            } finally { s.executeUpdate("UPDATE assistido SET nome='Pessoa Ficticia B' WHERE id=12"); }
        }
    }

    @Test void permiteNomeDaMaeAusenteSemInventarObrigatoriedade() throws Exception {
        try (var c = DriverManager.getConnection(BANCO); var s = c.createStatement()) {
            s.executeUpdate("UPDATE assistido SET nome_mae=NULL WHERE id=12");
            try {
                var response = chamar("assistidos/12/preparar", "ORG-TESTE", foto(), null);
                assertEquals(200, response.statusCode());
                assertTrue(new ObjectMapper().readTree(response.body()).get("nomeMae").isNull());
            } finally { s.executeUpdate("UPDATE assistido SET nome_mae='Mae Ficticia B' WHERE id=12"); }
        }
    }

    private HttpResponse<String> consultarHistorico(String rota) throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/sincronizacao/" + rota))
                    .GET().build(), HttpResponse.BodyHandlers.ofString());
        }
    }

    @Test void historicoGuardaSucessoSemFotoOuDadosPessoais() throws Exception {
        var response = chamar("assistidos/11/preparar", "ORG-TESTE", foto(), null);
        assertEquals(200, response.statusCode());
        var json = new ObjectMapper().readTree(response.body());
        String id = json.get("preparacaoId").asText();
        java.util.UUID.fromString(id);
        var consulta = consultarHistorico("preparacoes/" + id);
        assertEquals(200, consulta.statusCode());
        var resultado = new ObjectMapper().readTree(consulta.body());
        assertEquals("PREPARADO_LOCALMENTE", resultado.get("estado").asText());
        assertEquals(11, resultado.get("assistidoId").asInt());
        assertEquals(200, resultado.get("httpStatus").asInt());
        assertEquals(json.get("foto").get("sha256").asText(), resultado.get("fotoSha256").asText());
        assertFalse(resultado.has("foto")); assertFalse(resultado.has("base64"));
        assertFalse(resultado.has("cpf")); assertFalse(resultado.has("nome"));
        assertNotNull(resultado.get("criadoEm")); assertNotNull(resultado.get("atualizadoEm"));
    }

    @Test void falhaDePreparacaoPodeSerConsultadaPeloIdentificador() throws Exception {
        var response = chamar("assistidos/11/preparar", "ORG-TESTE", new byte[]{1,2,3}, null);
        assertEquals(400, response.statusCode());
        String id = new ObjectMapper().readTree(response.body()).get("preparacaoId").asText();
        var consulta = consultarHistorico("preparacoes/" + id);
        assertEquals(200, consulta.statusCode());
        var resultado = new ObjectMapper().readTree(consulta.body());
        assertEquals("FALHA_PREPARACAO", resultado.get("estado").asText());
        assertEquals("ENTRADA_INVALIDA", resultado.get("codigoErro").asText());
        assertEquals(400, resultado.get("httpStatus").asInt());
        assertTrue(resultado.get("mensagem").asText().contains("JPEG ou PNG"));
    }

    @Test void novaTentativaNaoApagaFalhaAnterior() throws Exception {
        var falha = chamar("assistidos/11/preparar", "ORG-TESTE", new byte[]{1,2,3}, null);
        var sucesso = chamar("assistidos/11/preparar", "ORG-TESTE", foto(), null);
        String idFalha = new ObjectMapper().readTree(falha.body()).get("preparacaoId").asText();
        String idSucesso = new ObjectMapper().readTree(sucesso.body()).get("preparacaoId").asText();
        assertNotEquals(idFalha, idSucesso);
        assertEquals("FALHA_PREPARACAO", new ObjectMapper().readTree(consultarHistorico("preparacoes/" + idFalha).body()).get("estado").asText());
        var lista = consultarHistorico("assistidos/11/preparacoes?limite=100");
        assertEquals(200, lista.statusCode());
        assertTrue(lista.body().contains(idFalha)); assertTrue(lista.body().contains(idSucesso));
    }

    @Test void historicoSobreviveANovaInstanciaDoServico() throws Exception {
        var response = chamar("assistidos/11/preparar", "ORG-TESTE", foto(), null, "ATUALIZACAO_FOTO");
        String id = new ObjectMapper().readTree(response.body()).get("preparacaoId").asText();
        var reaberto = new HistoricoPreparacaoService(jdbc);
        var resultado = reaberto.buscar(id);
        assertEquals("PREPARADO_LOCALMENTE", resultado.estado());
        assertEquals(FinalidadePreparacao.ATUALIZACAO_FOTO, resultado.finalidade());
    }

    @Test void estadoConcluidoNaoPodeSerSobrescritoPorFalha() {
        var historico = new HistoricoPreparacaoService(jdbc);
        String id = historico.iniciar(11L, FinalidadePreparacao.CADASTRO);
        assertEquals("EM_PREPARACAO", historico.buscar(id).estado());
        historico.concluir(id, "a".repeat(64));
        assertThrows(IllegalStateException.class, () -> historico.falhar(id, 400, "TESTE", "Teste"));
        assertEquals("PREPARADO_LOCALMENTE", historico.buscar(id).estado());
    }

    @Test void historicoValidaIdentificadoresELimites() throws Exception {
        assertEquals(404, consultarHistorico("preparacoes/" + java.util.UUID.randomUUID()).statusCode());
        assertEquals(400, consultarHistorico("preparacoes/id-invalido").statusCode());
        assertEquals(400, consultarHistorico("assistidos/11/preparacoes?limite=101").statusCode());
    }

    @Test void rejeicaoHttpAntesDoServicoNaoCriaTentativa() throws Exception {
        var historico = new HistoricoPreparacaoService(jdbc);
        historico.listar(11L, 1); // Garante a existencia da tabela no banco temporario.
        int antes = jdbc.queryForObject("SELECT COUNT(*) FROM integracao_preparacao", Integer.class);
        assertEquals(400, chamar("assistidos/11/preparar", "ORG-TESTE", null, null).statusCode());
        int depois = jdbc.queryForObject("SELECT COUNT(*) FROM integracao_preparacao", Integer.class);
        assertEquals(antes, depois);
    }

    @Test void interfaceServidaPelaMesmaAplicacao() throws Exception {
        try (var client = HttpClient.newHttpClient()) {
            for (String arquivo : new String[]{"pessoas.html", "script.js", "preparacao.js", "style.css"}) {
                var response = client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/interface/" + arquivo))
                        .GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(200, response.statusCode(), arquivo);
                if (arquivo.equals("pessoas.html")) {
                    assertTrue(response.body().contains("formPrepararFoto"));
                    assertTrue(response.body().contains("preparacao.js"));
                }
            }
        }
    }
}
