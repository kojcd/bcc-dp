package si.src.bcc.movies.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.movies.dto.LoginRequest;
import si.src.bcc.movies.util.JwtTokenGenerator;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final JwtTokenGenerator tokenGenerator;

    @Autowired
    public AuthController(JwtTokenGenerator tokenGenerator) {
        this.tokenGenerator = tokenGenerator;
    }

    @Operation(summary = "Get test token", description = "Generates a test JWT token for API testing - demo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generated successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/test-token")
    public ResponseEntity<String> getTestToken(
            @ParameterObject LoginRequest loginRequest) {

        // TODO: get JWT token with credentials, not implemented, just for demo purposes (suggested usage Keycloak)
        // For demo purposes, credentials from properties application.yml/application-docker.yml, token generated for demo
        if (tokenGenerator.getUsername().equals(loginRequest.getUsername()) &&
                tokenGenerator.getPassword().equals(loginRequest.getPassword())) {
            String token = tokenGenerator.generateToken(loginRequest.getUsername());
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}