package si.src.bcc.actors.util;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import si.src.bcc.actors.config.JwtConfig;
import si.src.bcc.actors.properties.JwtProperties;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenGenerator {

    private final SecretKey jwtKey;
    private final JwtProperties jwtProperties;

    @Autowired
    public JwtTokenGenerator(JwtConfig jwtConfig, JwtProperties jwtProperties) {
        this.jwtKey = jwtConfig.jwtKey();
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtKey)
                .compact();
    }

    public String getUsername() {
        return jwtProperties.getUsername();
    }

    public String getPassword() {
        return jwtProperties.getPassword();
    }
}