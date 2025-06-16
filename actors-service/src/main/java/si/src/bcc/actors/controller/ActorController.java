package si.src.bcc.actors.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.src.bcc.actors.dto.ActorRequest;
import si.src.bcc.actors.dto.ActorResponse;
import si.src.bcc.actors.exception.ActorAlreadyExistsException;
import si.src.bcc.actors.exception.ActorNotFoundException;
import si.src.bcc.actors.exception.InvalidActorDataException;
import si.src.bcc.actors.exception.NoActorsFoundException;
import si.src.bcc.actors.mapper.ActorMapper;
import si.src.bcc.actors.model.Actor;
import si.src.bcc.actors.service.ActorService;
import si.src.bcc.actors.service.impl.ActorServiceImpl;
import java.util.List;
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

    @Operation(summary = "Get all actors", description = "Retrieves a full list of all actors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actors retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/all")
    public ResponseEntity<List<ActorResponse>> getAllActorsWithoutPagination() {
        List<Actor> actors = actorService.getAllActors();
        if (actors.isEmpty()) {
            throw new NoActorsFoundException();
        }
        List<ActorResponse> response = actors.stream()
                .map(actorMapper::toResponse)
                .toList();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get all actors with pagination support", description = "Retrieves a paginated list of all actors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actors retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paged")
    public ResponseEntity<Page<ActorResponse>> getAllActors(
            @ParameterObject Pageable pageable) {
        Page<Actor> actors = actorService.getAllActors(pageable);
        if (actors.isEmpty()) {
            throw new NoActorsFoundException();
        }
        Page<ActorResponse> response = actors.map(actorMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get actor by ID", description = "Retrieves an actor by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actor retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Actor not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ActorResponse> getActorById(
            @Parameter(description = "Actor ID") @PathVariable Long id) {
        Actor actor = actorService.getActorById(id);
        if (actor == null) {
            throw new ActorNotFoundException(id);
        }
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(actorMapper.toResponse(actor));
    }

    @Operation(summary = "Create new actor", description = "Creates a new actor with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Actor created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid actor data"),
            @ApiResponse(responseCode = "409", description = "Actor already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ActorResponse> createActor(
            @Parameter(description = "Actor details") @Valid @RequestBody ActorRequest request) {
        if (actorService.existsByName(request.getFirstName(), request.getLastName())) {
            throw new ActorAlreadyExistsException(request.getFirstName(), request.getLastName());
        }
        Actor actor = actorMapper.toEntity(request);
        Actor createdActor = actorService.createActor(actor);
        if (createdActor == null) {
            throw new InvalidActorDataException("Failed to create actor");
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(actorMapper.toResponse(createdActor));
    }

    @Operation(summary = "Update actor", description = "Updates an existing actor's details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actor updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid actor data"),
            @ApiResponse(responseCode = "404", description = "Actor not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ActorResponse> updateActor(
            @Parameter(description = "Actor ID") @PathVariable Long id,
            @Parameter(description = "Updated actor details") @Valid @RequestBody ActorRequest request) {
        Actor actor = actorService.getActorById(id);
        if (actor == null) {
            throw new ActorNotFoundException(id);
        }
        actorMapper.updateEntity(actor, request);
        Actor updatedActor = actorService.updateActor(id, actor);
        if (updatedActor == null) {
            throw new InvalidActorDataException("Failed to update actor");
        }
        return ResponseEntity.ok(actorMapper.toResponse(updatedActor));
    }

    @Operation(summary = "Delete actor", description = "Deletes an actor by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Actor deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Actor not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActor(
            @Parameter(description = "Actor ID") @PathVariable Long id) {
        if (!actorService.existsById(id)) {
            throw new ActorNotFoundException(id);
        }
        boolean deleted = actorService.deleteActor(id);
        if (!deleted) {
            throw new InvalidActorDataException("Failed to delete actor");
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search actors with pagination support", description = "Searches actors by firstName or lastName with searchTerm and pagination support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<Page<ActorResponse>> searchActors(
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            @ParameterObject Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new InvalidActorDataException("searchTerm", "SearchTerm cannot be empty");
        }
        Page<Actor> actors = actorService.searchActors(searchTerm, pageable);
        if (actors.isEmpty()) {
            throw new NoActorsFoundException();
        }
        Page<ActorResponse> response = actors.map(actorMapper::toResponse);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES))
                .body(response);
    }

    @Operation(summary = "Get request statistics", description = "Retrieves the total number of requests made to the service")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Invalid or missing token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/stats/requests")
    public ResponseEntity<Long> getRequestCount() {
        return ResponseEntity.ok(actorServiceImpl.getRequestCount());
    }
}