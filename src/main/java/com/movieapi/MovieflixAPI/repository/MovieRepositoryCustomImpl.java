package com.movieapi.MovieflixAPI.repository;



import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.stereotype.Component;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

@Component
public class MovieRepositoryCustomImpl implements MovieRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public MovieRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Map> aggregateGenreCounts() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("$genre"),
                Aggregation.group("genre").count().as("count")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "movies", Map.class);
        return results.getMappedResults();
    }

    @Override
    public List<Map> aggregateRuntimeByYear() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("year").avg("runtime").as("avgRuntime"),
                Aggregation.sort(Sort.Direction.ASC, "_id")
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "movies", Map.class);
        return results.getMappedResults();
    }
}