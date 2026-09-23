package br.com.vianna.biblioteca.repository;
import br.com.vianna.biblioteca.domain.Emprestimo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
    long countByAlunoIdAndDataDevolucaoIsNull(Long alunoId);
    List<Emprestimo> findAllByOrderByDataEmprestimoDesc();
}
