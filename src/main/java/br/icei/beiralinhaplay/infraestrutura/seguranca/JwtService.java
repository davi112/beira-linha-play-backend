package br.icei.beiralinhaplay.infraestrutura.seguranca;

import br.icei.beiralinhaplay.dominio.autenticacao.AccessTokenProvider;
import br.icei.beiralinhaplay.dominio.usuario.Usuario;
import br.icei.beiralinhaplay.infraestrutura.configuracao.ApplicationProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService implements AccessTokenProvider {

    private final ApplicationProperties propriedades;

    public JwtService(ApplicationProperties propriedades) {
        this.propriedades = propriedades;
    }

    @Override
    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plusSeconds(propriedades.getJwt().getAccessTokenMinutos() * 60);
        return Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .claim("tipo", usuario.tipo().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expira))
                .signWith(chave())
                .compact();
    }

    @Override
    public ClaimsToken validar(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(chave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new ClaimsToken(UUID.fromString(claims.getSubject()), claims.get("tipo", String.class));
    }

    private SecretKey chave() {
        String secret = propriedades.getJwt().getSecret();
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(secret);
        } catch (Exception ex) {
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, Math.min(bytes.length, 32));
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
