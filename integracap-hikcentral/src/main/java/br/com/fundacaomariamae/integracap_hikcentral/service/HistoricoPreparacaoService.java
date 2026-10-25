package br.com.fundacaomariamae.integracap_hikcentral.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import br.com.fundacaomariamae.integracap_hikcentral.dto.FinalidadePreparacao;
import br.com.fundacaomariamae.integracap_hikcentral.dto.ResultadoPreparacao;

@Service
public class HistoricoPreparacaoService {
    private final JdbcTemplate jdbc;
    private volatile boolean inicializado;

    public HistoricoPreparacaoService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // Criada no primeiro uso. Nenhuma tabela anterior e alterada.
    private synchronized void inicializar() {
        if (inicializado) return;
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS integracao_preparacao (
                    id TEXT PRIMARY KEY,
                    assistido_id INTEGER NOT NULL,
                    finalidade TEXT NOT NULL,
                    estado TEXT NOT NULL CHECK (estado IN ('EM_PREPARACAO','PREPARADO_LOCALMENTE','FALHA_PREPARACAO')),
                    http_status INTEGER,
                    codigo_erro TEXT,
                    mensagem TEXT,
                    foto_sha256 TEXT,
                    criado_em TEXT NOT NULL,
                    atualizado_em TEXT NOT NULL
                )
                """);
        jdbc.execute("CREATE INDEX IF NOT EXISTS idx_integracao_preparacao_assistido ON integracao_preparacao(assistido_id, criado_em)");
        inicializado = true;
    }

    public String iniciar(Long assistidoId, FinalidadePreparacao finalidade) {
        inicializar();
        String id = UUID.randomUUID().toString();
        String agora = Instant.now().toString();
        jdbc.update("""
                INSERT INTO integracao_preparacao (id,assistido_id,finalidade,estado,criado_em,atualizado_em)
                VALUES (?,?,?,'EM_PREPARACAO',?,?)
                """, id, assistidoId, finalidade.name(), agora, agora);
        return id;
    }

    public void concluir(String id, String hash) {
        int alterados = jdbc.update("""
                UPDATE integracao_preparacao SET estado='PREPARADO_LOCALMENTE',http_status=200,
                    foto_sha256=?,atualizado_em=? WHERE id=? AND estado='EM_PREPARACAO'
                """, hash, Instant.now().toString(), id);
        if (alterados != 1) throw new IllegalStateException("Tentativa nao pode ser concluida");
    }

    public void falhar(String id, int status, String codigo, String mensagem) {
        int alterados = jdbc.update("""
                UPDATE integracao_preparacao SET estado='FALHA_PREPARACAO',http_status=?,
                    codigo_erro=?,mensagem=?,atualizado_em=? WHERE id=? AND estado='EM_PREPARACAO'
                """, status, codigo, mensagem, Instant.now().toString(), id);
        if (alterados != 1) throw new IllegalStateException("Tentativa nao pode ser marcada como falha");
    }

    public ResultadoPreparacao buscar(String id) {
        UUID.fromString(id);
        inicializar();
        var lista = jdbc.query("SELECT * FROM integracao_preparacao WHERE id=?", this::mapear, id);
        if (lista.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa nao encontrada");
        return lista.getFirst();
    }

    public List<ResultadoPreparacao> listar(Long assistidoId, int limite) {
        if (assistidoId <= 0 || limite < 1 || limite > 100) {
            throw new IllegalArgumentException("Informe ID positivo e limite entre 1 e 100");
        }
        inicializar();
        return jdbc.query("""
                SELECT * FROM integracao_preparacao WHERE assistido_id=?
                ORDER BY criado_em DESC,id DESC LIMIT ?
                """, this::mapear, assistidoId, limite);
    }

    private ResultadoPreparacao mapear(java.sql.ResultSet rs, int linha) throws java.sql.SQLException {
        int status = rs.getInt("http_status");
        Integer statusOpcional = rs.wasNull() ? null : status;
        return new ResultadoPreparacao(rs.getString("id"), rs.getLong("assistido_id"),
                FinalidadePreparacao.valueOf(rs.getString("finalidade")), rs.getString("estado"),
                statusOpcional, rs.getString("codigo_erro"), rs.getString("mensagem"),
                rs.getString("foto_sha256"), rs.getString("criado_em"), rs.getString("atualizado_em"));
    }
}
