package br.icei.beiralinhaplay.infraestrutura.seguranca;

import br.icei.beiralinhaplay.dominio.autenticacao.PasswordHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String codificar(String senhaCrua) {
        return encoder.encode(senhaCrua);
    }

    @Override
    public boolean confere(String senhaCrua, String senhaHash) {
        return encoder.matches(senhaCrua, senhaHash);
    }
}
