package br.com.vianna.biblioteca.repository;
import br.com.vianna.biblioteca.domain.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AlunoRepository extends JpaRepository<Aluno, Long> { }
