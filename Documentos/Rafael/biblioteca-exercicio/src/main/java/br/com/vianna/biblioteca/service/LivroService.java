package br.com.vianna.biblioteca.service;
import br.com.vianna.biblioteca.domain.Livro;
import br.com.vianna.biblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LivroService {
    private final LivroRepository repository;
    public LivroService(LivroRepository repository) { this.repository = repository; }
    public Livro cadastrar(Livro livro) { return repository.save(livro); }
    public List<Livro> listar() { return repository.findAll(); }
}
