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
import org.springframework.core.ParameterizedTypeReference;
import java.util.List;
import java.util.Map;

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

	@Test
	void testUpdateMovie() {
		// First create a movie
		MovieRequest request = new MovieRequest();
		request.setTitle("Original Title");
		request.setYear(Year.of(2024));
		request.setImdbId("tt1234567");
		request.setDescription("Original description");

		HttpEntity<MovieRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<MovieResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest,
				MovieResponse.class
		);
		MovieResponse savedMovie = createResponse.getBody();

		// Update the movie
		MovieRequest updateRequest = new MovieRequest();
		updateRequest.setTitle("Updated Title");
		updateRequest.setYear(Year.of(2024));
		updateRequest.setImdbId("tt1234567");
		updateRequest.setDescription("Updated description");

		HttpEntity<MovieRequest> updateRequestEntity = new HttpEntity<>(updateRequest, headers);
		ResponseEntity<MovieResponse> updateResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/" + savedMovie.getImdbId(),
				HttpMethod.PUT,
				updateRequestEntity,
				MovieResponse.class
		);

		// Verify update response
		assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(updateResponse.getBody()).isNotNull();
		assertThat(updateResponse.getBody().getTitle()).isEqualTo("Updated Title");
		assertThat(updateResponse.getBody().getDescription()).isEqualTo("Updated description");

		// Clean up
		movieRepository.deleteById(savedMovie.getImdbId());
	}

	@Test
	void testDeleteMovie() {
		// First create a movie
		MovieRequest request = new MovieRequest();
		request.setTitle("Temp Movie");
		request.setYear(Year.of(2024));
		request.setImdbId("tt1234567");
		request.setDescription("Temp description");

		HttpEntity<MovieRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<MovieResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest,
				MovieResponse.class
		);
		MovieResponse savedMovie = createResponse.getBody();

		// Delete the movie
		HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/" + savedMovie.getImdbId(),
				HttpMethod.DELETE,
				deleteRequest,
				Void.class
		);

		// Verify deletion
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		// Verify movie is deleted
		ResponseEntity<String> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/" + savedMovie.getImdbId(),
				HttpMethod.GET,
				new HttpEntity<>(headers),
				String.class
		);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testGetMovieNotFound() {
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/tt9999999",
				HttpMethod.GET,
				request,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testCreateMovieWithInvalidData() {
		MovieRequest invalidMovie = new MovieRequest();
		invalidMovie.setTitle("");
		invalidMovie.setYear(null);
		invalidMovie.setImdbId("invalid-imdb");
		invalidMovie.setDescription("");

		HttpEntity<MovieRequest> request = new HttpEntity<>(invalidMovie, headers);
		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				request,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void testUpdateNonExistingMovie() {
		MovieRequest updateRequest = new MovieRequest();
		updateRequest.setTitle("Updated Title");
		updateRequest.setYear(Year.of(2024));
		updateRequest.setImdbId("tt9999999");
		updateRequest.setDescription("Updated description");

		HttpEntity<MovieRequest> updateRequestEntity = new HttpEntity<>(updateRequest, headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/tt9999999",
				HttpMethod.PUT,
				updateRequestEntity,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testDeleteNonExistingMovie() {
		HttpEntity<Void> deleteRequest = new HttpEntity<>(headers);
		ResponseEntity<String> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/tt9999999",
				HttpMethod.DELETE,
				deleteRequest,
				String.class
		);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testSearchMovies() {
		// Create test movie
		MovieRequest request = new MovieRequest();
		request.setTitle("Testni Movie");
		request.setYear(Year.of(2024));
		request.setImdbId("tt1234567");
		request.setDescription("Search description");

		HttpEntity<MovieRequest> createRequest = new HttpEntity<>(request, headers);
		ResponseEntity<MovieResponse> createResponse = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest,
				MovieResponse.class
		);
		MovieResponse savedMovie = createResponse.getBody();

		// Test search
		HttpEntity<Void> searchRequest = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/search?searchTerm=Testni&page=0&size=10",
				HttpMethod.GET,
				searchRequest,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
		assertThat(content).hasSize(1);
		assertThat(content.get(0).get("title")).isEqualTo("Testni Movie");

		// Clean up
		movieRepository.deleteById(savedMovie.getImdbId());
	}

	@Test
	void testGetAllMoviesPaged() {
		// Create test movies
		MovieRequest request1 = new MovieRequest();
		request1.setTitle("Test Movie 1");
		request1.setYear(Year.of(2024));
		request1.setImdbId("tt1234567");
		request1.setDescription("Test description 1");

		HttpEntity<MovieRequest> createRequest1 = new HttpEntity<>(request1, headers);
		ResponseEntity<MovieResponse> createResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest1,
				MovieResponse.class
		);
		MovieResponse savedMovie1 = createResponse1.getBody();

		MovieRequest request2 = new MovieRequest();
		request2.setTitle("Test Movie 2");
		request2.setYear(Year.of(2024));
		request2.setImdbId("tt7654321");
		request2.setDescription("Test description 2");

		HttpEntity<MovieRequest> createRequest2 = new HttpEntity<>(request2, headers);
		ResponseEntity<MovieResponse> createResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest2,
				MovieResponse.class
		);
		MovieResponse savedMovie2 = createResponse2.getBody();

		// Test get all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/paged",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<Map<String, Object>>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
		assertThat(content).hasSizeGreaterThanOrEqualTo(2);

		// Clean up
		movieRepository.deleteById(savedMovie1.getImdbId());
		movieRepository.deleteById(savedMovie2.getImdbId());
	}

	@Test
	void testGetAllMoviesWithoutPagination() {
		// Create test movies
		MovieRequest request1 = new MovieRequest();
		request1.setTitle("Test Movie 1");
		request1.setYear(Year.of(2024));
		request1.setImdbId("tt1234567");
		request1.setDescription("Test description 1");

		HttpEntity<MovieRequest> createRequest1 = new HttpEntity<>(request1, headers);
		ResponseEntity<MovieResponse> createResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest1,
				MovieResponse.class
		);
		MovieResponse savedMovie1 = createResponse1.getBody();

		MovieRequest request2 = new MovieRequest();
		request2.setTitle("Test Movie 2");
		request2.setYear(Year.of(2024));
		request2.setImdbId("tt7654321");
		request2.setDescription("Test description 2");

		HttpEntity<MovieRequest> createRequest2 = new HttpEntity<>(request2, headers);
		ResponseEntity<MovieResponse> createResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/movies",
				createRequest2,
				MovieResponse.class
		);
		MovieResponse savedMovie2 = createResponse2.getBody();

		// Call /api/movies/all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/all",
				HttpMethod.GET,
				request,
				new ParameterizedTypeReference<>() {}
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).hasSizeGreaterThanOrEqualTo(2);

		// Clean up
		movieRepository.deleteById(savedMovie1.getImdbId());
		movieRepository.deleteById(savedMovie2.getImdbId());
	}

	@Test
	void testGetAllMoviesWithoutPaginationNoData() {
		// Ensure no movies exist
		movieRepository.deleteAll();

		// Call /api/movies/all
		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/api/movies/all",
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