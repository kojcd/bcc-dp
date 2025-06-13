package si.src.bcc.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import si.src.bcc.actors.config.TestJwtConfig;
import si.src.bcc.actors.dto.ActorRequest;
import si.src.bcc.actors.dto.ActorResponse;
import si.src.bcc.actors.repository.ActorRepository;
import si.src.bcc.actors.service.ActorService;
import si.src.bcc.actors.util.TestJwtUtil;
import java.time.LocalDate;
import java.util.HashSet;
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
		String token = jwtUtil.generateToken("test-user");
		headers.setBearerAuth(token);
	}
	@Test
	void testCreateAndGetActor() {
		// Create test data
		ActorRequest request = new ActorRequest();
		request.setFirstName("Testni");
		request.setLastName("Uporabnik");
		request.setBornDate(LocalDate.of(1980, 1, 1));
		Set<String> movies = new HashSet<>();
		movies.add("tt00882791");
		movies.add("tt00882101");
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
		assertThat(createResponse.getBody().getFirstName()).isEqualTo("Testni");
		assertThat(createResponse.getBody().getLastName()).isEqualTo("Uporabnik");
		assertThat(createResponse.getBody().getMovies()).hasSize(2);

		// Get the created actor's ID
		Long actorId = createResponse.getBody().getId();

		// Test actor retrieval
		HttpEntity<Void> getRequest = new HttpEntity<>(headers);
		ResponseEntity<ActorResponse> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/actors/" + actorId,
				org.springframework.http.HttpMethod.GET,
				getRequest,
				ActorResponse.class
		);

		// Verify retrieval response
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().getId()).isEqualTo(actorId);
		assertThat(getResponse.getBody().getFirstName()).isEqualTo("Testni");
		assertThat(getResponse.getBody().getLastName()).isEqualTo("Uporabnik");
		assertThat(getResponse.getBody().getBornDate()).isEqualTo(LocalDate.of(1980, 1, 1));
		assertThat(getResponse.getBody().getMovies()).hasSize(2);
		assertThat(getResponse.getBody().getMovies()).contains("tt00882791", "tt00882101");

		// Clean up
		actorRepository.deleteById(actorId);
	}
}
