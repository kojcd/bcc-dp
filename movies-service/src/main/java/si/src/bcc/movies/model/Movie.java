package si.src.bcc.movies.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "movies", schema = "movies")
public class Movie {
    @Id
    @NotBlank(message = "IMDB ID is required")
    @Size(min = 9, max = 10, message = "IMDB ID must be 9-10 characters")
    @Column(name = "imdb_id", nullable = false, unique = true)
    private String imdbId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull(message = "Year is required")
    @PastOrPresent(message = "Year must be in the past or present")
    @Column(name = "year", nullable = false)
    private Integer year;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "movie_actors", schema = "movies", joinColumns = @JoinColumn(name = "imdb_id"))
    @Column(name = "actor_id")
    private Set<Long> actors = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "movie_pictures", schema = "movies", joinColumns = @JoinColumn(name = "imdb_id"))
    @Column(name = "picture_url")
    private Set<String> pictures = new HashSet<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 