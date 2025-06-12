package si.src.bcc.actors.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.actors.util.JwtTokenGenerator;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final JwtTokenGenerator tokenGenerator;

    @Autowired
    public AuthController(JwtTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Operation(summary = "Get test token", description = "Generates a test JWT token for API testing")
    @GetMapping("/test-token")
    public ResponseEntity<String> getTestToken() {
        String token = tokenGenerator.generateToken("test-user");
        return ResponseEntity.ok(token);
    }
}