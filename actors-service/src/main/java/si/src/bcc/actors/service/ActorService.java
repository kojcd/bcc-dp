package si.src.bcc.actors.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import si.src.bcc.actors.model.Actor;

import java.util.List;

public interface ActorService {
    Page<Actor> getAllActors(Pageable pageable);
    List<Actor> getAllActors();
    Actor getActorById(Long id);
    Actor createActor(Actor actor);
    Actor updateActor(Long id, Actor actor);
    boolean deleteActor(Long id);
    Page<Actor> searchActors(String searchTerm, Pageable pageable);
    boolean existsById(Long id);
    boolean existsByName(String firstName, String lastName);
    long getRequestCount();
}