package com.movieapi.MovieflixAPI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OmdbMovie {

    @JsonProperty("imdbID")
    private String imdbID;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Year")
    private String year;

    public String getImdbID() { return imdbID; }
    public void setImdbID(String imdbID) { this.imdbID = imdbID; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
}
