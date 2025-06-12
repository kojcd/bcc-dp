package si.src.bcc.actors.service.impl;

import jakarta.persistence.EntityNotFoundException;
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
import java.util.concurrent.atomic.AtomicLong;

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
    @Cacheable(value = "actors", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Actor> getAllActors(Pageable pageable) {
        incrementRequestCounter();
        return actorRepository.findAll(pageable);
    }
    
    @Override
    @Cacheable(value = "actor", key = "#id")
    public Actor getActorById(Long id) {
        incrementRequestCounter();
        return actorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actor not found with id: " + id));
    }
    
    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public Actor createActor(Actor actor) {
        incrementRequestCounter();
        return actorRepository.save(actor);
    }
    
    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public Actor updateActor(Long id, Actor actor) {
        incrementRequestCounter();
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Actor not found with id: " + id);
        }
        actor.setId(id);
        return actorRepository.save(actor);
    }
    
    @Override
    @CacheEvict(value = {"actors", "actor"}, allEntries = true)
    public void deleteActor(Long id) {
        incrementRequestCounter();
        if (!actorRepository.existsById(id)) {
            throw new EntityNotFoundException("Actor not found with id: " + id);
        }
        actorRepository.deleteById(id);
    }
    
    @Override
    @Cacheable(value = "actorSearch", key = "#searchTerm + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Actor> searchActors(String searchTerm, Pageable pageable) {
        incrementRequestCounter();
        return actorRepository.searchActors(searchTerm, pageable);
    }
    
    private void incrementRequestCounter() {
        requestCounter.incrementAndGet();
    }
    
    public long getRequestCount() {
        return requestCounter.get();
    }
} 