package com.example.quanliphim;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.adapter.MovieAdapter;
import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private TextView btnBack, tvResultCount, tvEmptySearch;
    private EditText edtSearchKeyword;
    private ImageView btnClearSearch;
    private Spinner spinnerGenre;
    private RecyclerView rvSearchResults;

    private MovieAdapter movieAdapter;
    private MovieDatabase database;

    private LiveData<List<MovieEntity>> currentLiveData;
    private String selectedGenre = "Tất cả";
    private String currentKeyword = "";

    private List<String> genreList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_search);

        initViews();
        setupSpinner();
        setupRecyclerView();

        database = MovieDatabase.getDatabase(this);

        setupListeners();
        loadDistinctGenres();
        performSearch();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvResultCount = findViewById(R.id.tvResultCount);
        tvEmptySearch = findViewById(R.id.tvEmptySearch);
        edtSearchKeyword = findViewById(R.id.edtSearchKeyword);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        rvSearchResults = findViewById(R.id.rvSearchResults);

        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            edtSearchKeyword.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });
    }

    private void setupSpinner() {
        String[] defaultGenres = {"Tất cả", "Hành động", "Tình cảm", "Hài hước", "Kinh dị", "Viễn tưởng", "Hoạt hình", "Tâm lý", "Phiêu lưu", "Võ thuật", "Tài liệu", "Gia đình"};
        genreList.addAll(Arrays.asList(defaultGenres));

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genreList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenre.setAdapter(spinnerAdapter);

        spinnerGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGenre = genreList.get(position);
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRecyclerView() {
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(this::openMovieDetail);
        movieAdapter.setOnFavoriteClickListener(movie -> {
            movie.favorite = !movie.favorite;
            Executors.newSingleThreadExecutor().execute(() -> {
                database.movieDao().updateMovie(movie);
            });
        });
        rvSearchResults.setAdapter(movieAdapter);
    }

    private void setupListeners() {
        edtSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentKeyword = s.toString().trim();
                btnClearSearch.setVisibility(currentKeyword.isEmpty() ? View.GONE : View.VISIBLE);
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadDistinctGenres() {
        database.movieDao().getDistinctGenres().observe(this, genresFromDb -> {
            if (genresFromDb != null) {
                for (String g : genresFromDb) {
                    if (g != null && !g.trim().isEmpty() && !genreList.contains(g.trim())) {
                        genreList.add(g.trim());
                    }
                }
                spinnerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void performSearch() {
        if (currentLiveData != null) {
            currentLiveData.removeObservers(this);
        }

        currentLiveData = database.movieDao().searchAndFilterMovies(currentKeyword, selectedGenre);
        currentLiveData.observe(this, movies -> {
            if (movies == null || movies.isEmpty()) {
                tvEmptySearch.setVisibility(View.VISIBLE);
                rvSearchResults.setVisibility(View.GONE);
                tvResultCount.setText("Tìm thấy 0 phim");
            } else {
                tvEmptySearch.setVisibility(View.GONE);
                rvSearchResults.setVisibility(View.VISIBLE);
                movieAdapter.setMovieList(movies);
                tvResultCount.setText("Tìm thấy " + movies.size() + " phim");
            }
        });
    }

    private void openMovieDetail(MovieEntity movie) {
        Intent intent = new Intent(this, MovieDetailActivity.class);
        intent.putExtra("movie_id", movie.id);
        startActivity(intent);
    }
}
