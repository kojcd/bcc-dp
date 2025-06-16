package si.src.bcc.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import si.src.bcc.actors.config.TestJwtConfig;
import si.src.bcc.actors.dto.ActorRequest;
import si.src.bcc.actors.dto.ActorResponse;
import si.src.bcc.actors.repository.ActorRepository;
import si.src.bcc.actors.service.ActorService;
import si.src.bcc.actors.util.TestJwtUtil;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

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
		ActorRequest request = new ActorRequest();
		request.setFirstName("Test");
		request.setLastName("Actor");
		request.setBornDate(LocalDate.of(1990, 1, 1));
		Set<String> movies = new HashSet<>();
		movies.add("tt1234567");
		movies.add("tt7654321");
		request.setMovies(movies);

		// Test actor creation
		HttpEntity<ActorRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<ActorResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest,
				ActorResponse.class
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
		ResponseEntity<ActorResponse> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + actorId,
				HttpMethod.GET,
				getRequest,
				ActorResponse.class
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
		ActorRequest request = new ActorRequest();
		request.setFirstName("Original");
		request.setLastName("Name");
		request.setBornDate(LocalDate.of(1980, 1, 1));

		HttpEntity<ActorRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<ActorResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest,
				ActorResponse.class
		);
		ActorResponse savedActor = createResponse.getBody();

		// Update the actor
		ActorRequest updateRequest = new ActorRequest();
		updateRequest.setFirstName("Updated");
		updateRequest.setLastName("Name");
		updateRequest.setBornDate(LocalDate.of(1980, 1, 1));

		HttpEntity<ActorRequest> updateRequestEntity = new HttpEntity<>(updateRequest, headers);
		ResponseEntity<ActorResponse> updateResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + savedActor.getId(),
				HttpMethod.PUT,
				updateRequestEntity,
				ActorResponse.class
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
		ActorRequest request = new ActorRequest();
		request.setFirstName("Temp");
		request.setLastName("Actor");
		request.setBornDate(LocalDate.of(1995, 1, 1));

		HttpEntity<ActorRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<ActorResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest,
				ActorResponse.class
		);
		ActorResponse savedActor = createResponse.getBody();

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
		ActorRequest invalidRequest = new ActorRequest();
		invalidRequest.setFirstName("");
		invalidRequest.setLastName("");
		invalidRequest.setBornDate(LocalDate.now().plusDays(1)); // Future date

		HttpEntity<ActorRequest> request = new HttpEntity<>(invalidRequest, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				request,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void testUpdateNonExistingActor() {
		ActorRequest updateRequest = new ActorRequest();
		updateRequest.setFirstName("Updated");
		updateRequest.setLastName("Name");
		updateRequest.setBornDate(LocalDate.of(1980, 1, 1));

		HttpEntity<ActorRequest> updateRequestEntity = new HttpEntity<>(updateRequest, headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/999999",
				HttpMethod.PUT,
				updateRequestEntity,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testDeleteNonExistingActor() {
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
		ActorRequest request = new ActorRequest();
		request.setFirstName("Search");
		request.setLastName("Name");
		request.setBornDate(LocalDate.of(1990, 1, 1));

		HttpEntity<ActorRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<ActorResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest,
				ActorResponse.class
		);
		ActorResponse savedActor = createResponse.getBody();

		// Test search
		HttpEntity<Void> searchRequest = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/search?searchTerm=Search&page=0&size=10",
				HttpMethod.GET,
				searchRequest,
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
		ActorRequest request1 = new ActorRequest();
		request1.setFirstName("Test1");
		request1.setLastName("Actor1");
		request1.setBornDate(LocalDate.of(1990, 1, 1));

		HttpEntity<ActorRequest> createRequest1 = new HttpEntity<>(request1, headers);
		ResponseEntity<ActorResponse> createResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest1,
				ActorResponse.class
		);
		ActorResponse savedActor1 = createResponse1.getBody();

		ActorRequest request2 = new ActorRequest();
		request2.setFirstName("Test2");
		request2.setLastName("Actor2");
		request2.setBornDate(LocalDate.of(1991, 1, 1));

		HttpEntity<ActorRequest> createRequest2 = new HttpEntity<>(request2, headers);
		ResponseEntity<ActorResponse> createResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest2,
				ActorResponse.class
		);
		ActorResponse savedActor2 = createResponse2.getBody();

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
		ActorRequest request1 = new ActorRequest();
		request1.setFirstName("Test1");
		request1.setLastName("Actor1");
		request1.setBornDate(LocalDate.of(1990, 1, 1));

		HttpEntity<ActorRequest> createRequest1 = new HttpEntity<>(request1, headers);
		ResponseEntity<ActorResponse> createResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest1,
				ActorResponse.class
		);
		ActorResponse savedActor1 = createResponse1.getBody();

		ActorRequest request2 = new ActorRequest();
		request2.setFirstName("Test2");
		request2.setLastName("Actor2");
		request2.setBornDate(LocalDate.of(1991, 1, 1));

		HttpEntity<ActorRequest> createRequest2 = new HttpEntity<>(request2, headers);
		ResponseEntity<ActorResponse> createResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/actors",
				createRequest2,
				ActorResponse.class
		);
		ActorResponse savedActor2 = createResponse2.getBody();

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