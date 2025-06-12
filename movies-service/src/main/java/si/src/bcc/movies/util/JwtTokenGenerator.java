package si.src.bcc.movies.util;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import si.src.bcc.movies.config.JwtConfig;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenGenerator {

    private final SecretKey jwtKey;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Autowired
    public JwtTokenGenerator(JwtConfig jwtConfig) {
        this.jwtKey = jwtConfig.jwtKey();
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtKey)
                .compact();
    }
}