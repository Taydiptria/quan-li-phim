
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

    // Lấy toàn bộ danh sách phim
    @Query("SELECT * FROM movies ORDER BY id DESC")
    LiveData<List<MovieEntity>> getAllMovies();

    // Lấy phim mới thêm
    @Query("SELECT * FROM movies ORDER BY id DESC LIMIT 10")
    LiveData<List<MovieEntity>> getLatestMovies();

    // Lấy thông tin phim theo ID
    @Query("SELECT * FROM movies WHERE id = :movieId")
    LiveData<MovieEntity> getMovieById(int movieId);

    // Lấy danh sách phim yêu thích
    @Query("SELECT * FROM movies WHERE favorite = 1")
    LiveData<List<MovieEntity>> getFavoriteMovies();

    // Lấy danh sách phim đã xem
    @Query("SELECT * FROM movies WHERE watched = 1")
    LiveData<List<MovieEntity>> getWatchedMovies();

    // Thêm phim
    @Insert
    void insertMovie(MovieEntity movie);

    // Cập nhật phim
    @Update
    void updateMovie(MovieEntity movie);

    // Xóa phim
    @Delete
    void deleteMovie(MovieEntity movie);
}

