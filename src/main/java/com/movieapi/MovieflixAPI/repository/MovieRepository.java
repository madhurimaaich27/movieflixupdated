package com.movieapi.MovieflixAPI.repository;


import com.movieapi.MovieflixAPI.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie, String>, MovieRepositoryCustom {
    Movie findByImdbID(String imdbID);
    List<Movie> findByTitleContainingIgnoreCase(String title);
}