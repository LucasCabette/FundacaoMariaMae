package br.com.fundacaomariamae.integracap_hikcentral.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Leitura exclusiva da integracao, sem alterar a entidade nem o CRUD de assistidos. */
@Service
public class ConsultaCadastroIntegracaoService {
    private final JdbcTemplate jdbc;

    public record CadastroSalvo(Long id, String codigoAssistido, String nome,
                                 LocalDate dataNascimento, String nomeMae, String cpf, String status) {}

    public ConsultaCadastroIntegracaoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public CadastroSalvo buscarPorId(Long id) {
        var encontrados = jdbc.query("""
                SELECT id, codigo_assistido, nome, data_nascimento, nome_mae, cpf, status
                FROM assistido WHERE id = ?
                """, (rs, linha) -> new CadastroSalvo(rs.getLong("id"),
                rs.getString("codigo_assistido"), rs.getString("nome"),
                lerNascimento(rs.getObject("data_nascimento")),
                rs.getString("nome_mae"), rs.getString("cpf"), rs.getString("status")), id);
        if (encontrados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assistido nao encontrado");
        }
        return encontrados.getFirst();
    }

    private static LocalDate lerNascimento(Object valor) {
        // SQLite existente usa texto ISO; JDBC tambem pode gravar datas em milissegundos.
        if (valor instanceof Number numero) {
            return Instant.ofEpochMilli(numero.longValue()).atZone(ZoneOffset.UTC).toLocalDate();
        }
        if (valor != null) {
            String texto = valor.toString().strip();
            try {
                if (texto.matches("-?\\d+")) {
                    return Instant.ofEpochMilli(Long.parseLong(texto)).atZone(ZoneOffset.UTC).toLocalDate();
                }
                if (texto.length() == 10) return LocalDate.parse(texto);
                if (texto.length() > 10 && (texto.charAt(10) == ' ' || texto.charAt(10) == 'T')) {
                    return LocalDate.parse(texto.substring(0, 10));
                }
            } catch (DateTimeParseException | NumberFormatException ignored) { /* Retorna erro de dados, sem corrigi-los. */ }
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Data de nascimento do cadastro salvo ausente ou em formato nao reconhecido");
    }
}
