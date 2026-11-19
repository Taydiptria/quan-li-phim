package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MovieLibraryActivity extends AppCompatActivity {

    TextView btnBack, tvEmpty, tvTotalCount;
    ImageView btnAddMovie;
    EditText edtSearch;
    RecyclerView rvAllMovies;

    MovieAdapter movieAdapter;
    MovieDatabase database;

    private List<MovieEntity> fullMovieList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_movie_library);

        // System Window Insets handling (Status bar / Bottom bar overlap protection)
        View mainRoot = findViewById(R.id.mainRoot);
        if (mainRoot != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainRoot, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        btnBack = findViewById(R.id.btnBack);
        btnAddMovie = findViewById(R.id.btnAddMovie);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        edtSearch = findViewById(R.id.edtSearch);
        rvAllMovies = findViewById(R.id.rvAllMovies);

        btnBack.setOnClickListener(v -> finish());
        btnAddMovie.setOnClickListener(v -> {
            Intent intent = new Intent(MovieLibraryActivity.this, AddMovieActivity.class);
            startActivity(intent);
        });

        rvAllMovies.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(this::openMovieDetail);
        movieAdapter.setOnFavoriteClickListener(movie -> {
            movie.favorite = !movie.favorite;
            Executors.newSingleThreadExecutor().execute(() -> database.movieDao().updateMovie(movie));
        });
        rvAllMovies.setAdapter(movieAdapter);

        // Search text watcher
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMovies(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        database = MovieDatabase.getDatabase(this);
        database.movieDao().getAllMovies().observe(this, movies -> {
            fullMovieList = movies != null ? movies : new ArrayList<>();
            filterMovies(edtSearch.getText().toString());
        });
    }

    private void filterMovies(String query) {
        if (fullMovieList == null || fullMovieList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Chưa có bộ phim nào trong kho phim.");
            rvAllMovies.setVisibility(View.GONE);
            tvTotalCount.setText("Tổng số phim: 0");
            return;
        }

        String search = query == null ? "" : query.trim().toLowerCase();
        List<MovieEntity> filteredList = new ArrayList<>();

        for (MovieEntity m : fullMovieList) {
            boolean matchesTitle = m.title != null && m.title.toLowerCase().contains(search);
            boolean matchesGenre = m.genre != null && m.genre.toLowerCase().contains(search);

            if (search.isEmpty() || matchesTitle || matchesGenre) {
                filteredList.add(m);
            }
        }

        tvTotalCount.setText("Tổng số phim: " + filteredList.size() + " / " + fullMovieList.size());

        if (filteredList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Không tìm thấy phim phù hợp với từ khóa \"" + query + "\"");
            rvAllMovies.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvAllMovies.setVisibility(View.VISIBLE);
            movieAdapter.setMovieList(filteredList);
        }
    }

    private void openMovieDetail(MovieEntity movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
