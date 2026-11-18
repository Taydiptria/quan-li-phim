package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.concurrent.Executors;

public class FavoriteActivity extends AppCompatActivity {

    TextView btnBack, tvEmpty;
    RecyclerView rvFavoriteMovies;
    MovieAdapter movieAdapter;
    MovieDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_favorite);

        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        btnBack = findViewById(R.id.btnBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvFavoriteMovies = findViewById(R.id.rvFavoriteMovies);

        btnBack.setOnClickListener(v -> finish());

        rvFavoriteMovies.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(this::openMovieDetail);
        movieAdapter.setOnFavoriteClickListener(movie -> {
            movie.favorite = !movie.favorite;
            Executors.newSingleThreadExecutor().execute(() -> {
                database.movieDao().updateMovie(movie);
            });
        });
        rvFavoriteMovies.setAdapter(movieAdapter);

        database = MovieDatabase.getDatabase(this);
        database.movieDao().getFavoriteMovies().observe(this, movies -> {
            if (movies == null || movies.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvFavoriteMovies.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvFavoriteMovies.setVisibility(View.VISIBLE);
                movieAdapter.setMovieList(movies);
            }
        });
    }

    private void openMovieDetail(MovieEntity movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
