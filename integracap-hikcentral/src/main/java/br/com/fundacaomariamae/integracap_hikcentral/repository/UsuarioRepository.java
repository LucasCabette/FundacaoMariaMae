
package br.com.fundacaomariamae.integracap_hikcentral.repository;

import br.com.fundacaomariamae.integracap_hikcentral.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findByEmailAndAtivoTrue(String email);
}
