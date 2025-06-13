package si.src.bcc.actors.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import javax.crypto.SecretKey;

@TestConfiguration
public class TestJwtConfig {

    private static final SecretKey testKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Bean
    @Primary
    public SecretKey jwtKey() {
        return testKey;
    }
}
