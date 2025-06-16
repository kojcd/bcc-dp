package si.src.bcc.movies.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Set;

@Data
public class MovieResponse {
    private String imdbId;
    private String title;
    private String description;
    private Set<Long> actors;
    private Set<String> pictures;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
    @Schema(type = "string", example = "2025", pattern = "^\\d{4}$")
    private Year year;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Schema(type = "string", format = "date-time", example = "2025-06-16T14:50:57.543Z")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    @Schema(type = "string", format = "date-time", example = "2025-06-16T14:50:57.543Z")
    private LocalDateTime updatedAt;
}