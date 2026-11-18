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

public class FavoriteActivity extends AppCompatActivity {

    private TextView btnBack, tvEmpty, tvFavoriteCount;
    private EditText edtFavoriteSearch;
    private RecyclerView rvFavoriteMovies;
    private MovieAdapter movieAdapter;
    private MovieDatabase database;

    private List<MovieEntity> fullFavoriteList = new ArrayList<>();
    private String currentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_favorite);

        btnBack = findViewById(R.id.btnBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvFavoriteCount = findViewById(R.id.tvFavoriteCount);
        edtFavoriteSearch = findViewById(R.id.edtFavoriteSearch);
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

        edtFavoriteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim().toLowerCase();
                filterFavoriteList();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        database = MovieDatabase.getDatabase(this);
        database.movieDao().getFavoriteMovies().observe(this, movies -> {
            fullFavoriteList = movies != null ? movies : new ArrayList<>();
            filterFavoriteList();
        });
    }

    private void filterFavoriteList() {
        if (fullFavoriteList.isEmpty()) {
            tvEmpty.setText("Bạn chưa thêm bộ phim nào vào danh sách yêu thích.");
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavoriteMovies.setVisibility(View.GONE);
            tvFavoriteCount.setText("Tổng số: 0 phim yêu thích");
            return;
        }

        List<MovieEntity> filteredList = new ArrayList<>();
        if (currentKeyword.isEmpty()) {
            filteredList.addAll(fullFavoriteList);
        } else {
            for (MovieEntity movie : fullFavoriteList) {
                if (movie.title != null && movie.title.toLowerCase().contains(currentKeyword)) {
                    filteredList.add(movie);
                }
            }
        }

        if (filteredList.isEmpty()) {
            tvEmpty.setText("Không tìm thấy phim yêu thích nào khớp với từ khóa \"" + currentKeyword + "\"");
            tvEmpty.setVisibility(View.VISIBLE);
            rvFavoriteMovies.setVisibility(View.GONE);
            tvFavoriteCount.setText("Tìm thấy 0 phim khớp tìm kiếm");
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFavoriteMovies.setVisibility(View.VISIBLE);
            movieAdapter.setMovieList(filteredList);
            tvFavoriteCount.setText("Tổng số: " + filteredList.size() + " phim yêu thích");
        }
    }

    private void openMovieDetail(MovieEntity movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
