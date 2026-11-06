package br.com.fundacaomariamae.integracap_hikcentral.repository;

import br.com.fundacaomariamae.integracap_hikcentral.model.Assistido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AssistidoRepository extends JpaRepository<Assistido, Long> {
    Optional<Assistido> findByCodigoAssistido(String codigoAssistido);
    Optional<Assistido> findByCpf(String cpf);
    List<Assistido> findByNomeContainingIgnoreCase(String nome);
    default boolean existsByNomeIgnoreCaseAndDataNascimento(String nome, LocalDate dataNascimento) {
        return existeNomeENascimentoNormalizado(nome, dataNascimento.toString()) > 0;
    }

    // Mantem o criterio nome+nascimento para datas antigas e novas, sem migrar registros.
    @Query(value = """
            SELECT COUNT(*) FROM assistido
            WHERE lower(nome) = lower(:nome)
              AND CASE WHEN substr(data_nascimento, 5, 1) = '-'
                       THEN substr(data_nascimento, 1, 10)
                       ELSE strftime('%Y-%m-%d', CAST(data_nascimento AS INTEGER) / 1000.0, 'unixepoch')
                  END = :nascimento
            """, nativeQuery = true)
    long existeNomeENascimentoNormalizado(@Param("nome") String nome, @Param("nascimento") String nascimento);
}
