package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.FavouriteMovieAdapter;
import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvMovies, rvFavoriteMovies;
    NestedScrollView mainScrollView;

    TextView tvEmpty, tvFavoriteEmpty;
    ImageView btnAddMovie, btnStatistics, btnProfile;

    View btnNavHome, btnNavFavorites, btnNavWatched, btnNavStats, btnNavLibrary;

    MovieAdapter movieAdapter;
    FavouriteMovieAdapter favoriteAdapter;

    MovieDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_main);

        // Window insets to prevent status bar and bottom gesture bar overlaps
        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mainScrollView = findViewById(R.id.mainScrollView);
        rvMovies = findViewById(R.id.rvMovies);
        rvFavoriteMovies = findViewById(R.id.rvFavoriteMovies);

        tvEmpty = findViewById(R.id.tvEmpty);
        tvFavoriteEmpty = findViewById(R.id.tvFavoriteEmpty);

        btnAddMovie = findViewById(R.id.btnAddMovie);
        btnStatistics = findViewById(R.id.btnStatistics);
        btnProfile = findViewById(R.id.btnProfile);

        // Bottom Navigation Tabs
        btnNavHome = findViewById(R.id.btnNavHome);
        btnNavFavorites = findViewById(R.id.btnNavFavorites);
        btnNavWatched = findViewById(R.id.btnNavWatched);
        btnNavStats = findViewById(R.id.btnNavStats);
        btnNavLibrary = findViewById(R.id.btnNavLibrary);

        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMovieActivity.class);
            startActivity(intent);
        });

        View.OnClickListener openStats = v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        };
        btnStatistics.setOnClickListener(openStats);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Setup Bottom Nav Listeners
        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> {
                if (mainScrollView != null) {
                    mainScrollView.smoothScrollTo(0, 0);
                }
            });
        }

        if (btnNavFavorites != null) {
            btnNavFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
                startActivity(intent);
            });
        }

        if (btnNavWatched != null) {
            btnNavWatched.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WatchedActivity.class);
                startActivity(intent);
            });
        }

        if (btnNavStats != null) {
            btnNavStats.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);
            });
        }

        if (btnNavLibrary != null) {
            btnNavLibrary.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MovieLibraryActivity.class);
                startActivity(intent);
            });
        }

        // Danh sách phim mới thêm: hiển thị dọc
        rvMovies.setLayoutManager(new LinearLayoutManager(this));

        // Danh sách phim yêu thích: vuốt ngang
        rvFavoriteMovies.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Adapter phim mới thêm
        movieAdapter = new MovieAdapter(this::openMovieDetail);
        rvMovies.setAdapter(movieAdapter);
        movieAdapter.setOnFavoriteClickListener(movie -> {
            movie.favorite = !movie.favorite;
            Executors.newSingleThreadExecutor().execute(() -> database.movieDao().updateMovie(movie));
        });

        // Adapter phim yêu thích
        favoriteAdapter = new FavouriteMovieAdapter(this::openMovieDetail);
        rvFavoriteMovies.setAdapter(favoriteAdapter);

        // Kết nối Database
        database = MovieDatabase.getDatabase(this);

        // Lấy phim mới thêm
        database.movieDao().getAllMovies().observe(this, movies -> {
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
        database.movieDao().getFavoriteMovies().observe(this, movies -> {
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
        Intent intent = new Intent(MainActivity.this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
