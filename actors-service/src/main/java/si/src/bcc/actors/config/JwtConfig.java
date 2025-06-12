package si.src.bcc.actors.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.crypto.SecretKey;

@Configuration
public class JwtConfig {

    @Bean
    public SecretKey jwtKey() {
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
}
