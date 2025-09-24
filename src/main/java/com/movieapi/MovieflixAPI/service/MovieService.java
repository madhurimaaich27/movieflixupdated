package com.movieapi.MovieflixAPI.service;

import com.movieapi.MovieflixAPI.dto.OmdbMovie;
import com.movieapi.MovieflixAPI.dto.OmdbMovieDetails;
import com.movieapi.MovieflixAPI.dto.OmdbSearchResponse;
import com.movieapi.MovieflixAPI.model.Movie;
import com.movieapi.MovieflixAPI.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${omdb.api.key}")
    private String apiKey;

    private static final String OMDB_API_URL = "http://www.omdbapi.com/?apikey=";

    public Movie getMovieByImdbId(String imdbId) {
        logger.info("Fetching movie with IMDb ID: {}", imdbId);
        // Check MongoDB first
        Optional<Movie> movieOptional = movieRepository.findByImdbID(imdbId);
        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();
            // Refresh if stale or null fetchedAt
            if (movie.getFetchedAt() == null || 
                movie.getFetchedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                logger.info("Movie data is stale, refreshing from OMDB: {}", imdbId);
                return fetchMovieDetailsAndSave(imdbId);
            }
            logger.info("Found movie in MongoDB: {}", imdbId);
            return movie;
        }

        // Fetch from OMDB API if not in MongoDB
        logger.info("Movie not found in MongoDB, fetching from OMDB: {}", imdbId);
        return fetchMovieDetailsAndSave(imdbId);
    }

    public List<Movie> fetchAndCacheMovies(String search) {
        String url = String.format("%s%s&s=%s", OMDB_API_URL, apiKey, search);
        OmdbSearchResponse response = restTemplate.getForObject(url, OmdbSearchResponse.class);
        List<OmdbMovie> movies = response != null && response.getSearch() != null ? response.getSearch() : List.of();

        for (OmdbMovie omdbMovie : movies) {
            Optional<Movie> existing = movieRepository.findByImdbID(omdbMovie.getImdbID());
            boolean isStale = existing.isPresent() &&
                    (existing.get().getFetchedAt() == null ||
                     existing.get().getFetchedAt().isBefore(LocalDateTime.now().minusHours(24)));

            if (!existing.isPresent() || isStale) {
                fetchMovieDetailsAndSave(omdbMovie.getImdbID());
            }
        }

        return movieRepository.findByTitleContainingIgnoreCase(search);
    }

    public Movie getMovieById(String imdbId) {
        Optional<Movie> movieOptional = movieRepository.findByImdbID(imdbId);
        if (movieOptional.isPresent()) {
            Movie movie = movieOptional.get();
            // Refresh if stale or null fetchedAt
            if (movie.getFetchedAt() == null ||
                movie.getFetchedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                logger.info("Movie data is stale, refreshing from OMDB: {}", imdbId);
                return fetchMovieDetailsAndSave(imdbId);
            }
            return movie;
        }
        return fetchMovieDetailsAndSave(imdbId);
    }

    private Movie fetchMovieDetailsAndSave(String imdbId) {
        String detailsUrl = String.format("%s%s&i=%s", OMDB_API_URL, apiKey, imdbId);
        OmdbMovieDetails details = restTemplate.getForObject(detailsUrl, OmdbMovieDetails.class);

        if (details != null && "True".equals(details.getResponse())) {
            Movie movie = new Movie();
            movie.setImdbID(details.getImdbID());
            movie.setTitle(details.getTitle() != null ? details.getTitle() : "Unknown");
            movie.setYear(details.getYear() != null && !details.getYear().equals("N/A") ?
                    Integer.parseInt(details.getYear()) : 0);
            movie.setGenre(details.getGenre() != null && !details.getGenre().equals("N/A") ?
                    Arrays.asList(details.getGenre().split(", ")) : List.of());
            movie.setDirector(details.getDirector() != null ? details.getDirector() : "Unknown");
            movie.setActors(details.getActors() != null && !details.getActors().equals("N/A") ?
                    Arrays.asList(details.getActors().split(", ")) : List.of());
            movie.setRating(details.getImdbRating() != null && !details.getImdbRating().equals("N/A") ?
                    Double.parseDouble(details.getImdbRating()) : 0.0);
            movie.setRuntime(details.getRuntime() != null && !details.getRuntime().equals("N/A") ?
                    parseRuntime(details.getRuntime()) : 0);
            movie.setPlot(details.getPlot() != null ? details.getPlot() : "");
            movie.setFetchedAt(LocalDateTime.now());

            movieRepository.save(movie);
            logger.info("Saved movie to MongoDB: {}", imdbId);
            return movie;
        } else {
            logger.warn("OMDB API returned no movie for IMDb ID: {}", imdbId);
            return null;
        }
    }

    private int parseRuntime(String runtime) {
        try {
            return Integer.parseInt(runtime.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            logger.warn("Invalid runtime format: {}", runtime);
            return 0;
        }
    }

    public void initializeFetchedAtForExistingMovies() {
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            if (movie.getFetchedAt() == null) {
                movie.setFetchedAt(LocalDateTime.now());
                movieRepository.save(movie);
            }
        }
    }
}