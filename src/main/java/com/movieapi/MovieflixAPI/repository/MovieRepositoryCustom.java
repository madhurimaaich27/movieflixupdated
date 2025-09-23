package com.movieapi.MovieflixAPI.repository;


import java.util.List;
import java.util.Map;

public interface MovieRepositoryCustom {
    List<Map> aggregateGenreCounts();
    List<Map> aggregateRuntimeByYear();
}