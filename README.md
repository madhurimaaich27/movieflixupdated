Movieflix API

Movieflix API is a Spring Boot application that provides movie data by integrating with the OMDB API and storing results in MongoDB Atlas. 
It supports endpoints for fetching movie details by IMDb ID, searching movies, and retrieving aggregates (e.g., genre counts, average ratings).

Live API: https://movieflixupdated-1.onrender.com

Features

Fetch movie details by IMDb ID (/api/movies/{imdbid}).

Search movies by title (/api/movies?search=query).

Retrieve all movies or aggregated statistics (/api/movies/aggregates).

Cache movie data in MongoDB Atlas for performance.

Deployed on Render with Docker.

Prerequisites

Java 17: Required for Spring Boot.

Maven: For building the project.

MongoDB Atlas: Free M0 cluster for database storage.

OMDB API Key: Obtain from http://www.omdbapi.com/apikey.aspx.

Docker: For containerized deployment (optional for local testing).

Git: For version control.

Project Structure

movieflixupdated/

├── src/

│   ├── main/

│   │   ├── java/com/movieapi/MovieflixAPI/

│   │   │   ├── controller/MovieController.java

│   │   │   ├── dto/OmdbMovieDetails.java

│   │   │   ├── model/Movie.java

│   │   │   ├── repository/MovieRepository.java

│   │   │   ├── service/MovieService.java

│   │   ├── resources/application.properties

├── Dockerfile

├── pom.xml

├── README.md



Test the Live API

IMDb Lookup:
curl https://movieflixupdated-1.onrender.com/api/movies/tt0133093

Expected: JSON for The Matrix.

Search:
curl https://movieflixupdated-1.onrender.com/api/movies?search=Matrix

Expected: Array of movie JSONs.

All Movies:
curl https://movieflixupdated-1.onrender.com/api/movies

Aggregates:
curl https://movieflixupdated-1.onrender.com/api/movies/aggregates
