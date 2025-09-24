package com.movieapi.MovieflixAPI.repository;


import com.movieapi.MovieflixAPI.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;
import java.util.List;


public interface MovieRepository extends MongoRepository<Movie, String>, MovieRepositoryCustom {
    Optional<Movie> findByImdbID(String imdbId);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}