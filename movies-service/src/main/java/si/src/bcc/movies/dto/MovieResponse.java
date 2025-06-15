package si.src.bcc.movies.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Set;

@Data
public class MovieResponse {
    private String imdbId;
    private String title;
    private Year year;
    private String description;
    private Set<Long> actors;
    private Set<String> pictures;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}