package com.example.quanliphim.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import com.example.quanliphim.entity.MovieEntity;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY id DESC")
    LiveData<List<MovieEntity>> getAllMovies();

    @Query("SELECT COUNT(*) FROM movies")
    int getMovieCountSync();

    @Query("SELECT * FROM movies ORDER BY id DESC LIMIT 10")
    LiveData<List<MovieEntity>> getLatestMovies();

    @Query("SELECT * FROM movies WHERE id = :movieId")
    LiveData<MovieEntity> getMovieById(int movieId);

    @Query("SELECT * FROM movies WHERE favorite = 1")
    LiveData<List<MovieEntity>> getFavoriteMovies();

    @Query("SELECT * FROM movies WHERE favorite = 0 ORDER BY id DESC")
    LiveData<List<MovieEntity>> getNonFavoriteMovies();

    @Query("DELETE FROM movies WHERE id NOT IN (SELECT MAX(id) FROM movies GROUP BY title)")
    void deleteDuplicateMoviesSync();

    @Query("SELECT * FROM movies WHERE watched = 1")
    LiveData<List<MovieEntity>> getWatchedMovies();

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<MovieEntity>> searchMovies(String query);

    @Query("SELECT * FROM movies WHERE genre LIKE '%' || :genre || '%' ORDER BY id DESC")
    LiveData<List<MovieEntity>> filterMoviesByGenre(String genre);

    @Query("SELECT * FROM movies WHERE (:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%') AND (:genre IS NULL OR :genre = '' OR :genre = 'Tất cả' OR genre LIKE '%' || :genre || '%') ORDER BY id DESC")
    LiveData<List<MovieEntity>> searchAndFilterMovies(String query, String genre);

    @Query("SELECT DISTINCT genre FROM movies WHERE genre IS NOT NULL AND genre != ''")
    LiveData<List<String>> getDistinctGenres();

    @Insert
    void insertMovie(MovieEntity movie);

    @Update
    void updateMovie(MovieEntity movie);

    @Delete
    void deleteMovie(MovieEntity movie);
}
