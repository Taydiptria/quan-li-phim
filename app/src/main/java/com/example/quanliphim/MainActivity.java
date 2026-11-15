package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.adapter.FavouriteMovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvMovies, rvFavoriteMovies;

    TextView tvEmpty, tvFavoriteEmpty;
    ImageView btnAddMovie;

    MovieAdapter movieAdapter;
    FavouriteMovieAdapter favoriteAdapter;

    MovieDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvMovies = findViewById(R.id.rvMovies);
        rvFavoriteMovies = findViewById(R.id.rvFavoriteMovies);

        tvEmpty = findViewById(R.id.tvEmpty);
        tvFavoriteEmpty = findViewById(R.id.tvFavoriteEmpty);
        btnAddMovie = findViewById(R.id.btnAddMovie);

        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMovieActivity.class);
            startActivity(intent);
        });

        // Danh sách phim mới thêm: hiển thị dọc
        rvMovies.setLayoutManager(
                new LinearLayoutManager(this));

        // Danh sách phim yêu thích: vuốt ngang
        rvFavoriteMovies.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                ));

        // Adapter phim mới thêm
        movieAdapter = new MovieAdapter(movie ->
                openMovieDetail(movie));

        rvMovies.setAdapter(movieAdapter);
        movieAdapter.setOnFavoriteClickListener(movie -> {

            movie.favorite = !movie.favorite;

            java.util.concurrent.Executors
                    .newSingleThreadExecutor()
                    .execute(() -> {
                        database.movieDao().updateMovie(movie);
                    });

        });

        // Adapter phim yêu thích
        favoriteAdapter = new FavouriteMovieAdapter(movie ->
                openMovieDetail(movie));

        rvFavoriteMovies.setAdapter(favoriteAdapter);

        // Kết nối Database
        database = MovieDatabase.getDatabase(this);

        // Lấy phim mới thêm
        database.movieDao().getAllMovies()
                .observe(this, movies -> {

                    if (movies == null || movies.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvMovies.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvMovies.setVisibility(View.VISIBLE);
                        movieAdapter.setMovieList(movies);
                    }
                });

        // Lấy phim yêu thích
        database.movieDao().getFavoriteMovies()
                .observe(this, movies -> {

                    if (movies == null || movies.isEmpty()) {
                        tvFavoriteEmpty.setVisibility(View.VISIBLE);
                        rvFavoriteMovies.setVisibility(View.GONE);
                    } else {
                        tvFavoriteEmpty.setVisibility(View.GONE);
                        rvFavoriteMovies.setVisibility(View.VISIBLE);
                        favoriteAdapter.setMovies(movies);
                    }
                });
    }

    private void openMovieDetail(MovieEntity movie) {

        Intent intent = new Intent(
                MainActivity.this,
                MovieDetailActivity.class
        );

        intent.putExtra("movie_id", movie.id);

        startActivity(intent);
    }
}
