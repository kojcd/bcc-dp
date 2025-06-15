package si.src.bcc.movies;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import si.src.bcc.movies.config.TestJwtConfig;
import si.src.bcc.movies.dto.MovieRequest;
import si.src.bcc.movies.dto.MovieResponse;
import si.src.bcc.movies.repository.MovieRepository;
import si.src.bcc.movies.service.MovieService;
import si.src.bcc.movies.util.TestJwtUtil;

import java.time.Year;
import java.util.HashSet;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.main.allow-bean-definition-overriding=true"
})
@Import({TestJwtConfig.class})
class MoviesServiceApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MovieService movieService;

	@Autowired
	private MovieRepository movieRepository;

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
	void testCreateAndGetMovie() {
		// Create test data
		MovieRequest request = new MovieRequest();
		request.setTitle("Testni Film");
		request.setYear(Year.of(2024));
		request.setImdbId("tt1234567");
		request.setDescription("Testni opis filma");
		Set<Long> actors = new HashSet<>();
		actors.add(1L);
		actors.add(2L);
		request.setActors(actors);
		Set<String> pictures = new HashSet<>();
		pictures.add("http://example.com/picture1.jpg");
		pictures.add("http://example.com/picture2.jpg");
		request.setPictures(pictures);

		// Test movie creation
		HttpEntity<MovieRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<MovieResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest,
				MovieResponse.class
		);

		// Verify creation response
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createResponse.getBody()).isNotNull();
		assertThat(createResponse.getBody().getTitle()).isEqualTo("Testni Film");
		assertThat(createResponse.getBody().getImdbId()).isEqualTo("tt1234567");
		assertThat(createResponse.getBody().getYear()).isEqualTo(Year.of(2024));
		assertThat(createResponse.getBody().getDescription()).isEqualTo("Testni opis filma");
		assertThat(createResponse.getBody().getActors()).hasSize(2);
		assertThat(createResponse.getBody().getPictures()).hasSize(2);

		// Get the created movie's ID
		String movieId = createResponse.getBody().getImdbId();

		// Test movie retrieval
		HttpEntity<Void> getRequest = new HttpEntity<>(headers);
		ResponseEntity<MovieResponse> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/" + movieId,
				org.springframework.http.HttpMethod.GET,
				getRequest,
				MovieResponse.class
		);

		// Verify retrieval response
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(getResponse.getBody()).isNotNull();
		assertThat(getResponse.getBody().getImdbId()).isEqualTo(movieId);
		assertThat(getResponse.getBody().getTitle()).isEqualTo("Testni Film");
		assertThat(getResponse.getBody().getYear()).isEqualTo(Year.of(2024));
		assertThat(getResponse.getBody().getDescription()).isEqualTo("Testni opis filma");
		assertThat(getResponse.getBody().getActors()).hasSize(2);
		assertThat(getResponse.getBody().getActors()).contains(1L, 2L);
		assertThat(getResponse.getBody().getPictures()).hasSize(2);
		assertThat(getResponse.getBody().getPictures()).contains("http://example.com/picture1.jpg", "http://example.com/picture2.jpg");

		// Clean up
		movieRepository.deleteById(movieId);
	}
}