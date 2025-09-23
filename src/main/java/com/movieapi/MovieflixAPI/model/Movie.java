package com.movieapi.MovieflixAPI.model;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "movies")
public class Movie {
    @Id
    private String imdbID;
    private String title;
    private int year;
    private List<String> genre;
    private String director;
    private List<String> actors;
    private double rating;
    private int runtime;
    private String plot;
    private LocalDateTime fetchedAt;

    public Movie() {}

    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public List<String> getGenre() { return genre; }
    public void setGenre(List<String> genre) { this.genre = genre; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public List<String> getActors() { return actors; }
    public void setActors(List<String> actors) { this.actors = actors; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public int getRuntime() { return runtime; }
    public void setRuntime(int runtime) { this.runtime = runtime; }
    public String getPlot() { return plot; }
    public void setPlot(String plot) { this.plot = plot; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}