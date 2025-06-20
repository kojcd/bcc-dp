package si.src.bcc.actors.mapper;

import org.springframework.stereotype.Component;
import si.src.bcc.actors.dto.ActorRequest;
import si.src.bcc.actors.dto.ActorResponse;
import si.src.bcc.actors.model.Actor;
import java.util.HashSet;
import java.util.Optional;

@Component
public class ActorMapper {
    
    public Actor toEntity(ActorRequest request) {
        Actor actor = new Actor();
        actor.setFirstName(request.getFirstName());
        actor.setLastName(request.getLastName());
        actor.setBornDate(request.getBornDate());
        actor.setMovies(Optional.ofNullable(request.getMovies()).orElseGet(HashSet::new));
        return actor;
    }
    
    public ActorResponse toResponse(Actor actor) {
        ActorResponse response = new ActorResponse();
        response.setId(actor.getId());
        response.setFirstName(actor.getFirstName());
        response.setLastName(actor.getLastName());
        response.setBornDate(actor.getBornDate());
        response.setMovies(Optional.ofNullable(actor.getMovies()).orElseGet(HashSet::new));
        response.setCreatedAt(actor.getCreatedAt());
        response.setUpdatedAt(actor.getUpdatedAt());
        return response;
    }
    
    public void updateEntity(Actor actor, ActorRequest request) {
        actor.setFirstName(request.getFirstName());
        actor.setLastName(request.getLastName());
        actor.setBornDate(request.getBornDate());
        actor.setMovies(Optional.ofNullable(request.getMovies()).orElseGet(HashSet::new));
    }
} 