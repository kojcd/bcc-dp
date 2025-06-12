package si.src.bcc.actors.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ActorResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate bornDate;
    private Set<String> movies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 