package com.movieapi.MovieflixAPI.service;



import com.movieapi.MovieflixAPI.model.Movie;
import com.movieapi.MovieflixAPI.repository.MovieRepository;
import com.movieapi.MovieflixAPI.dto.OmdbMovie;
import com.movieapi.MovieflixAPI.dto.OmdbMovieDetails;
import com.movieapi.MovieflixAPI.dto.OmdbSearchResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final RestTemplate restTemplate;
    private final String apiKey = "YOUR_OMDB_API_KEY"; // replace with your key

    public MovieService(MovieRepository movieRepository, RestTemplate restTemplate) {
        this.movieRepository = movieRepository;
        this.restTemplate = restTemplate;
    }

    public List<Movie> fetchAndCacheMovies(String search) {
        String url = String.format("http://www.omdbapi.com/?apikey=%s&s=%s", apiKey, search);
        OmdbSearchResponse response = restTemplate.getForObject(url, OmdbSearchResponse.class);
        List<OmdbMovie> movies = response != null ? response.getSearch() : List.of();

        for (OmdbMovie omdbMovie : movies) {
            Movie existing = movieRepository.findByImdbID(omdbMovie.getImdbID());
            boolean isStale = existing != null &&
                    (existing.getFetchedAt() == null ||
                     existing.getFetchedAt().isBefore(LocalDateTime.now().minusHours(24)));

            if (existing == null || isStale) {
                String detailsUrl = String.format("http://www.omdbapi.com/?apikey=%s&i=%s", apiKey, omdbMovie.getImdbID());
                OmdbMovieDetails details = restTemplate.getForObject(detailsUrl, OmdbMovieDetails.class);

                if (details != null) {
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
                            Integer.parseInt(details.getRuntime().replace(" min", "")) : 0);
                    movie.setPlot(details.getPlot() != null ? details.getPlot() : "");
                    movie.setFetchedAt(LocalDateTime.now());
                    movieRepository.save(movie);
                }
            }
        }

        return movieRepository.findByTitleContainingIgnoreCase(search);
    }

    public Movie getMovieById(String imdbID) {
        Movie movie = movieRepository.findByImdbID(imdbID);

        if (movie != null) {
            // Refresh if stale or null fetchedAt
            if (movie.getFetchedAt() == null ||
                movie.getFetchedAt().isBefore(LocalDateTime.now().minusHours(24))) {
                fetchAndCacheMovies(movie.getTitle());
                movie = movieRepository.findByImdbID(imdbID); // reload
            }
        }

        return movie;
    }

    // Optional: fix old movies with null fetchedAt
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
