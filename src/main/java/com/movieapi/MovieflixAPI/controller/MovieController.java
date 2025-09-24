package com.movieapi.MovieflixAPI.controller;

import com.movieapi.MovieflixAPI.model.Movie;
import com.movieapi.MovieflixAPI.service.MovieService;
import com.movieapi.MovieflixAPI.repository.MovieRepository;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"https://your-netlify-site.netlify.app", "http://localhost:8085"})
@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private static final Logger logger = LoggerFactory.getLogger(MovieController.class);
    private final MovieService movieService;
    private final MovieRepository movieRepository;

    @Autowired
    public MovieController(MovieService movieService, MovieRepository movieRepository) {
        this.movieService = movieService;
        this.movieRepository = movieRepository;
    }

    @GetMapping
    public List<Movie> searchMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String filter) {
        if (search != null) {
            movieService.fetchAndCacheMovies(search);
        }
        List<Movie> movies = movieRepository.findAll();
        if (filter != null && filter.startsWith("genre:")) {
            String genre = filter.split(":")[1];
            movies = movies.stream()
                    .filter(m -> m.getGenre().contains(genre))
                    .collect(Collectors.toList());
        }
        if (sort != null) {
            String[] parts = sort.split(":");
            String field = parts[0];
            Sort.Direction direction = parts.length > 1 && parts[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            movies = movieRepository.findAll(Sort.by(direction, field));
        }
        return movies;
    }

    @GetMapping("/{imdbid}")
    public ResponseEntity<Movie> getMovieByImdbId(@PathVariable("imdbid") String imdbId) {
        logger.info("Requested IMDb ID: {}", imdbId);
        Movie movie = movieService.getMovieByImdbId(imdbId);
        if (movie == null) {
            logger.warn("Movie not found for IMDb ID: {}", imdbId);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(movie);
    }

    @GetMapping("/aggregates")
    public Map<String, Object> getAggregates() {
        List<Map> genreCounts = movieRepository.aggregateGenreCounts();
        double avgRating = movieRepository.findAll().stream()
                .mapToDouble(Movie::getRating)
                .average()
                .orElse(0.0);
        List<Map> avgRuntimeByYear = movieRepository.aggregateRuntimeByYear();
        List<Movie> movies = movieRepository.findAll();
        return Map.of(
                "genreCounts", genreCounts,
                "avgRating", avgRating,
                "avgRuntimeByYear", avgRuntimeByYear,
                "movies", movies
        );
    }

    @GetMapping("/download")
    public void downloadMovies(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=movies.csv");

        List<Movie> movies = movieRepository.findAll();
        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            writer.writeNext(new String[]{"imdbID", "title", "year", "rating", "runtime", "genre", "director", "actors", "plot"});
            for (Movie movie : movies) {
                writer.writeNext(new String[]{
                    movie.getImdbID(),
                    movie.getTitle(),
                    String.valueOf(movie.getYear()),
                    String.valueOf(movie.getRating()),
                    String.valueOf(movie.getRuntime()),
                    String.join(", ", movie.getGenre()),
                    movie.getDirector(),
                    String.join(", ", movie.getActors()),
                    movie.getPlot()
                });
            }
        }
    }
}