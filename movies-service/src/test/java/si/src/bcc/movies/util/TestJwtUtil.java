package si.src.bcc.movies.util;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import si.src.bcc.movies.config.TestJwtConfig;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class TestJwtUtil {

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private final SecretKey testKey;

    @Autowired
    public TestJwtUtil(TestJwtConfig jwtConfig) {
        this.testKey = jwtConfig.jwtKey();
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(testKey)
                .compact();
    }
}
