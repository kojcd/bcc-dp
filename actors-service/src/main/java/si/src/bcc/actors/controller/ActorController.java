package si.src.bcc.actors.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.actors.dto.ActorRequest;
import si.src.bcc.actors.dto.ActorResponse;
import si.src.bcc.actors.mapper.ActorMapper;
import si.src.bcc.actors.model.Actor;
import si.src.bcc.actors.service.ActorService;
import si.src.bcc.actors.service.impl.ActorServiceImpl;
import java.util.concurrent.TimeUnit;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/actors")
@Tag(name = "Actor Management", description = "APIs for managing actors")
public class ActorController {

    private final ActorService actorService;
    private final ActorServiceImpl actorServiceImpl;
    private final ActorMapper actorMapper;

    @Autowired
    public ActorController(ActorService actorService, ActorServiceImpl actorServiceImpl, ActorMapper actorMapper) {
        this.actorService = actorService;
        this.actorServiceImpl = actorServiceImpl;
        this.actorMapper = actorMapper;
    }

    @Operation(summary = "Get all actors", description = "Retrieves a paginated list of all actors")
    @GetMapping
    public ResponseEntity<Page<ActorResponse>> getAllActors(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        Page<Actor> actors = actorService.getAllActors(pageable);
        if (actors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Page<ActorResponse> response = actors.map(actorMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get actor by ID", description = "Retrieves an actor by their ID")
    @GetMapping("/{id}")
    public ResponseEntity<ActorResponse> getActorById(
            @Parameter(description = "Actor ID") @PathVariable Long id) {
        Actor actor = actorService.getActorById(id);
        if (actor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(actorMapper.toResponse(actor));
    }

    @Operation(summary = "Create new actor", description = "Creates a new actor with the provided details")
    @PostMapping
    public ResponseEntity<ActorResponse> createActor(
            @Parameter(description = "Actor details") @Valid @RequestBody ActorRequest request) {
        Actor actor = actorMapper.toEntity(request);
        Actor createdActor = actorService.createActor(actor);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(actorMapper.toResponse(createdActor));
    }

    @Operation(summary = "Update actor", description = "Updates an existing actor's details")
    @PutMapping("/{id}")
    public ResponseEntity<ActorResponse> updateActor(
            @Parameter(description = "Actor ID") @PathVariable Long id,
            @Parameter(description = "Updated actor details") @Valid @RequestBody ActorRequest request) {
        Actor actor = actorService.getActorById(id);
        if (actor == null) {
            return ResponseEntity.notFound().build();
        }
        actorMapper.updateEntity(actor, request);
        Actor updatedActor = actorService.updateActor(id, actor);
        return ResponseEntity.ok(actorMapper.toResponse(updatedActor));
    }

    @Operation(summary = "Delete actor", description = "Deletes an actor by their ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActor(
            @Parameter(description = "Actor ID") @PathVariable Long id) {
        boolean deleted = actorService.deleteActor(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search actors", description = "Searches actors by name with pagination")
    @GetMapping("/search")
    public ResponseEntity<Page<ActorResponse>> searchActors(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Page<Actor> actors = actorService.searchActors(searchTerm, pageable);
        if (actors.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        Page<ActorResponse> response = actors.map(actorMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get request statistics", description = "Retrieves the total number of requests made to the service")
    @GetMapping("/stats/requests")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(actorServiceImpl.getRequestCount());
    }
}