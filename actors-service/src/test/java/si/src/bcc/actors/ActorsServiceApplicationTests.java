package si.src.bcc.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import si.src.bcc.actors.config.TestJwtConfig;
import si.src.bcc.actors.util.TestJwtUtil;
import si.src.bcc.actors.model.Actor;
import si.src.bcc.actors.repository.ActorRepository;
import si.src.bcc.actors.service.ActorService;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.main.allow-bean-definition-overriding=true"
})
@Import({TestJwtConfig.class})
class ActorsServiceApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ActorService actorService;

	@Autowired
	private ActorRepository actorRepository;

	@Autowired
	private TestJwtUtil jwtUtil;

	private HttpHeaders headers;

	@BeforeEach
	void setUp() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(jwtUtil.generateToken("test-user"));
	}

	@Test
	void testCreateAndGetActor() {
		// Create test data
		Actor actor = new Actor();
		actor.setFirstName("Test");
		actor.setLastName("Actor");
		actor.setBornDate(LocalDate.of(1990, 1, 1));
		Set<String> movies = new HashSet<>();
		movies.add("tt1234567");
		movies.add("tt7654321");
		actor.setMovies(movies);

		// Test actor creation
		HttpEntity<Actor> createRequest = new HttpEntity<>(actor, headers);
		ResponseEntity<Actor> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest,
				Actor.class
		);

		// Verify creation response
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getBody()).isNotNull();
		assertThat(createResponse.getBody().getFirstName()).isEqualTo("Test");
		assertThat(createResponse.getBody().getLastName()).isEqualTo("Actor");
		assertThat(createResponse.getBody().getBornDate()).isEqualTo(LocalDate.of(1990, 1, 1));
		assertThat(createResponse.getBody().getMovies()).hasSize(2);
		assertThat(createResponse.getBody().getMovies()).contains("tt1234567", "tt7654321");

		// Get the created actor's ID
		Long actorId = createResponse.getBody().getId();

		// Test actor retrieval
		HttpEntity<Void> getRequest = new HttpEntity<>(headers);
		ResponseEntity<Actor> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + actorId,
				HttpMethod.GET,
				getRequest,
				Actor.class
		);

		// Verify retrieval response
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().getId()).isEqualTo(actorId);
		assertThat(getResponse.getBody().getFirstName()).isEqualTo("Test");
		assertThat(getResponse.getBody().getLastName()).isEqualTo("Actor");
		assertThat(getResponse.getBody().getBornDate()).isEqualTo(LocalDate.of(1990, 1, 1));
		assertThat(getResponse.getBody().getMovies()).hasSize(2);
		assertThat(getResponse.getBody().getMovies()).contains("tt1234567", "tt7654321");

		// Clean up
		actorRepository.deleteById(actorId);
	}

	@Test
	void testUpdateActor() {
		// First create an actor
		Actor actor = new Actor();
		actor.setFirstName("Original");
		actor.setLastName("Name");
		actor.setBornDate(LocalDate.of(1980, 1, 1));
		Actor savedActor = actorService.createActor(actor);

		// Update the actor
		Actor updateActor = new Actor();
		updateActor.setFirstName("Updated");
		updateActor.setLastName("Name");
		updateActor.setBornDate(LocalDate.of(1980, 1, 1));

		HttpEntity<Actor> updateRequest = new HttpEntity<>(updateActor, headers);
		ResponseEntity<Actor> updateResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + savedActor.getId(),
				HttpMethod.PUT,
				updateRequest,
				Actor.class
		);

		// Verify update response
		assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(updateResponse.getBody()).isNotNull();
		assertThat(updateResponse.getBody().getFirstName()).isEqualTo("Updated");
		assertThat(updateResponse.getBody().getLastName()).isEqualTo("Name");

		// Clean up
		actorRepository.deleteById(savedActor.getId());
	}

	@Test
	void testDeleteActor() {
		// First create an actor
		Actor actor = new Actor();
		actor.setFirstName("Temp");
		actor.setLastName("Actor");
		actor.setBornDate(LocalDate.of(1995, 1, 1));
		Actor savedActor = actorService.createActor(actor);

		// Delete the actor
		HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + savedActor.getId(),
				HttpMethod.DELETE,
				deleteRequest,
				Void.class
		);

		// Verify deletion
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// Verify actor is deleted
		ResponseEntity<String> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + savedActor.getId(),
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class
		);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testGetActorNotFound() {
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/999999",
				HttpMethod.GET,
				request,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testCreateActorWithInvalidData() {
		Actor invalidActor = new Actor();
		invalidActor.setFirstName("");
		invalidActor.setLastName("");
		invalidActor.setMovies(null);
		invalidActor.setBornDate(LocalDate.now().plusDays(1)); // Future date

		HttpEntity<Actor> request = new HttpEntity<>(invalidActor, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				request,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void testUpdateNonExistingActor() {
		// Clean up
		actorRepository.deleteById(999999L);

		Actor updateActor = new Actor();
		updateActor.setFirstName("Updated");
		updateActor.setLastName("Name");
		updateActor.setBornDate(LocalDate.of(1980, 1, 1));

		HttpEntity<Actor> updateRequest = new HttpEntity<>(updateActor, headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/999999",
				HttpMethod.PUT,
				updateRequest,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testDeleteNonExistingActor() {
		// Clean up
		actorRepository.deleteById(999999L);

		HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/999999",
				HttpMethod.DELETE,
				deleteRequest,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testSearchActors() {
		// Create test actor
		Actor actor = new Actor();
		actor.setFirstName("Search");
		actor.setLastName("Name");
		actor.setBornDate(LocalDate.of(1990, 1, 1));
		Actor savedActor = actorService.createActor(actor);

		// Test search
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/search?searchTerm=Search&page=0&size=10",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
		assertThat(content).hasSize(1);
		assertThat(content.get(0).get("firstName")).isEqualTo("Search");

		// Clean up
		actorRepository.deleteById(savedActor.getId());
	}

	@Test
	void testGetAllActorsPaged() {
		// Create test actors
		Actor actor1 = new Actor();
		actor1.setFirstName("Test1");
		actor1.setLastName("Actor1");
		actor1.setBornDate(LocalDate.of(1990, 1, 1));
		Actor savedActor1 = actorService.createActor(actor1);

		Actor actor2 = new Actor();
		actor2.setFirstName("Test2");
		actor2.setLastName("Actor2");
		actor2.setBornDate(LocalDate.of(1991, 1, 1));
		Actor savedActor2 = actorService.createActor(actor2);

		// Test get all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/paged",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
		assertThat(content).hasSizeGreaterThanOrEqualTo(2);

		// Clean up
		actorRepository.deleteById(savedActor1.getId());
		actorRepository.deleteById(savedActor2.getId());
	}

	@Test
	void testGetAllActorsWithoutPagination() {
		// Create test actors
		Actor actor1 = new Actor();
		actor1.setFirstName("Test1");
		actor1.setLastName("Actor1");
		actor1.setBornDate(LocalDate.of(1990, 1, 1));
		Actor savedActor1 = actorService.createActor(actor1);

		Actor actor2 = new Actor();
		actor2.setFirstName("Test2");
		actor2.setLastName("Actor2");
		actor2.setBornDate(LocalDate.of(1991, 1, 1));
		Actor savedActor2 = actorService.createActor(actor2);

		// Call /api/actors/all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/all",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);

		// Clean up
		actorRepository.deleteById(savedActor1.getId());
		actorRepository.deleteById(savedActor2.getId());
	}

	@Test
	void testGetAllActorsWithoutPaginationNoData() {
		// Ensure no actors exist
		actorRepository.deleteAll();

		// Call /api/actors/all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/all",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
		assertThat(content).isNull();
	}
}