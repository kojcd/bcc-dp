package si.src.bcc.movies.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.Year;
import java.util.Set;

@Data
public class MovieRequest {
    @NotBlank(message = "IMDB ID is required")
    @Size(min = 9, max = 10, message = "IMDB ID must be 9-10 characters")
    private String imdbId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @NotNull(message = "Year is required")
    @PastOrPresent(message = "Year must be in the past or present")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy")
    @Schema(type = "string", example = "2025", pattern = "^\\d{4}$")
    private Year year;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Set<Long> actors;

    private Set<String> pictures;
}