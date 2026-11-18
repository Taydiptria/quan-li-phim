
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

    // Tìm kiếm phim theo tên
    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' ORDER BY id DESC")
    LiveData<List<MovieEntity>> searchMovies(String query);

    // Lọc phim theo thể loại
    @Query("SELECT * FROM movies WHERE genre LIKE '%' || :genre || '%' ORDER BY id DESC")
    LiveData<List<MovieEntity>> filterMoviesByGenre(String genre);

    // Tìm kiếm kết hợp lọc thể loại
    @Query("SELECT * FROM movies WHERE (:query IS NULL OR :query = '' OR title LIKE '%' || :query || '%') AND (:genre IS NULL OR :genre = '' OR :genre = 'Tất cả' OR genre LIKE '%' || :genre || '%') ORDER BY id DESC")
    LiveData<List<MovieEntity>> searchAndFilterMovies(String query, String genre);

    // Lấy danh sách thể loại không trùng lặp
    @Query("SELECT DISTINCT genre FROM movies WHERE genre IS NOT NULL AND genre != ''")
    LiveData<List<String>> getDistinctGenres();

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

