package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class WatchedActivity extends AppCompatActivity {

    private TextView btnBack, tvEmpty, tvWatchedCount;
    private EditText edtWatchedSearch;
    private RecyclerView rvWatchedMovies;
    private MovieAdapter movieAdapter;
    private MovieDatabase database;

    private List<MovieEntity> fullWatchedList = new ArrayList<>();
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_watched);

        btnBack = findViewById(R.id.btnBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvWatchedCount = findViewById(R.id.tvWatchedCount);
        edtWatchedSearch = findViewById(R.id.edtWatchedSearch);
        rvWatchedMovies = findViewById(R.id.rvWatchedMovies);

        btnBack.setOnClickListener(v -> finish());

        rvWatchedMovies.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(this::openMovieDetail);
        movieAdapter.setOnFavoriteClickListener(movie -> {
            movie.favorite = !movie.favorite;
            Executors.newSingleThreadExecutor().execute(() -> {
                database.movieDao().updateMovie(movie);
            });
        });
        rvWatchedMovies.setAdapter(movieAdapter);

        edtWatchedSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim().toLowerCase();
                filterWatchedList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        database = MovieDatabase.getDatabase(this);
        database.movieDao().getWatchedMovies().observe(this, movies -> {
            fullWatchedList = movies != null ? movies : new ArrayList<>();
            filterWatchedList();
        });
    }

    private void filterWatchedList() {
        if (fullWatchedList.isEmpty()) {
            tvEmpty.setText("Bạn chưa đánh dấu bộ phim nào là đã xem.");
            tvEmpty.setVisibility(View.VISIBLE);
            rvWatchedMovies.setVisibility(View.GONE);
            tvWatchedCount.setText("Tổng số: 0 phim đã xem");
            return;
        }

        List<MovieEntity> filteredList = new ArrayList<>();
        if (currentKeyword.isEmpty()) {
            filteredList.addAll(fullWatchedList);
        } else {
            for (MovieEntity movie : fullWatchedList) {
                if (movie.title != null && movie.title.toLowerCase().contains(currentKeyword)) {
                    filteredList.add(movie);
                }
            }
        }

        if (filteredList.isEmpty()) {
            tvEmpty.setText("Không tìm thấy phim đã xem nào khớp với từ khóa \"" + currentKeyword + "\"");
            tvEmpty.setVisibility(View.VISIBLE);
            rvWatchedMovies.setVisibility(View.GONE);
            tvWatchedCount.setText("Tìm thấy 0 phim khớp tìm kiếm");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvWatchedMovies.setVisibility(View.VISIBLE);
            movieAdapter.setMovieList(filteredList);
            tvWatchedCount.setText("Tổng số: " + filteredList.size() + " phim đã xem");
        }
    }

    private void openMovieDetail(MovieEntity movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
