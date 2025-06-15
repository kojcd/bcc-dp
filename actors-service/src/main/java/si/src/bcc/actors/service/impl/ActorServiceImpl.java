package si.src.bcc.actors.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.src.bcc.actors.model.Actor;
import si.src.bcc.actors.repository.ActorRepository;
import si.src.bcc.actors.service.ActorService;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@Transactional
public class ActorServiceImpl implements ActorService {

    private final ActorRepository actorRepository;
    private final AtomicLong requestCounter = new AtomicLong(0);

    @Autowired
    public ActorServiceImpl(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    @Override
    @Cacheable(value = "actors", key = "'all'")
    public List<Actor> getAllActors() {
        log.debug("Fetching all actors");
        incrementRequestCounter();
        List<Actor> actors = actorRepository.findAll();
        log.debug("Found {} actors", actors.size());
        return actors;
    }

    @Override
    @Cacheable(value = "actors", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Actor> getAllActors(Pageable pageable) {
        log.debug("Fetching all actors with page: {} and size: {}", pageable.getPageNumber(), pageable.getPageSize());
        incrementRequestCounter();
        Page<Actor> actors = actorRepository.findAll(pageable);
        log.debug("Found {} actors", actors.getTotalElements());
        return actors;
    }

    @Override
    @Cacheable(value = "actor", key = "#id")
    public Actor getActorById(Long id) {
        log.debug("Fetching actor with id: {}", id);
        incrementRequestCounter();
        Actor actor = actorRepository.findById(id).orElse(null);
        if (actor != null) {
            log.debug("Found actor: {} {}", actor.getFirstName(), actor.getLastName());
        } else {
            log.debug("No actor found with id: {}", id);
        }
        return actor;
    }

    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public Actor createActor(Actor actor) {
        log.debug("Creating new actor: {} {}", actor.getFirstName(), actor.getLastName());
        incrementRequestCounter();
        Actor savedActor = actorRepository.save(actor);
        log.info("Created actor with id: {} and name: {} {}", savedActor.getId(), savedActor.getFirstName(), savedActor.getLastName());
        return savedActor;
    }

    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public Actor updateActor(Long id, Actor actor) {
        log.debug("Updating actor with id: {}", id);
        incrementRequestCounter();
        if (!actorRepository.existsById(id)) {
            log.debug("No actor found with id: {} for update", id);
            return null;
        }
        actor.setId(id);
        Actor updatedActor = actorRepository.save(actor);
        log.info("Updated actor with id: {} and name: {} {}", updatedActor.getId(), updatedActor.getFirstName(), updatedActor.getLastName());
        return updatedActor;
    }

    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public boolean deleteActor(Long id) {
        log.debug("Attempting to delete actor with id: {}", id);
        incrementRequestCounter();
        if (!actorRepository.existsById(id)) {
            log.debug("No actor found with id: {} for deletion", id);
            return false;
        }
        actorRepository.deleteById(id);
        log.info("Successfully deleted actor with id: {}", id);
        return true;
    }

    @Override
    @Cacheable(value = "actorSearch", key = "#searchTerm + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Actor> searchActors(String searchTerm, Pageable pageable) {
        incrementRequestCounter();
        return actorRepository.searchActors(searchTerm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return actorRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String firstName, String lastName) {
        return actorRepository.existsByFirstNameAndLastName(firstName, lastName);
    }

    private void incrementRequestCounter() {
        long count = requestCounter.incrementAndGet();
        log.debug("Request counter incremented to: {}", count);
    }

    @Override
    public long getRequestCount() {
        log.debug("Request counter returned: {}", requestCounter.get());
        return requestCounter.get();
    }
}