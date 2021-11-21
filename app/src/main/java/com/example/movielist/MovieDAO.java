package com.example.movielist;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MovieDAO {

    @Insert
    void insertMovie(Movie movie);

    @Query("SELECT * FROM movie_table")
    List<Movie> getAll();

    @Query("SELECT title FROM movie_table")
    List<String> getAllTitles();

    @Query("SELECT * FROM movie_table WHERE title = :title")
    Movie findByTitle(String title);

}
