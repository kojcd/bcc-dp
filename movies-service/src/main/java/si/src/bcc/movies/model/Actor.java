package si.src.bcc.movies.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "actors")
public class Actor {
    @Id
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "born_date")
    private LocalDate bornDate;

    @ElementCollection
    @CollectionTable(name = "actor_movies", joinColumns = @JoinColumn(name = "actor_id"))
    @Column(name = "movie_imdb_id")
    private Set<String> movies = new HashSet<>();
}
