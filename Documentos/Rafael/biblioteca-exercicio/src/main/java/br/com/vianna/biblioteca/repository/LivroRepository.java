package br.com.vianna.biblioteca.repository;
import br.com.vianna.biblioteca.domain.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LivroRepository extends JpaRepository<Livro, Long> { }
