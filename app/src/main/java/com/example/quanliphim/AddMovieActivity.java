package com.example.quanliphim;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMovieActivity extends AppCompatActivity {

    EditText edtTitle, edtGenre, edtDirector, edtYear, edtRating, edtPoster, edtDescription;
    Button btnSaveMovie;
    TextView btnBack;

    MovieDatabase database;
    int movieId = -1;
    MovieEntity currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_movie);

        edtTitle = findViewById(R.id.edtTitle);
        edtGenre = findViewById(R.id.edtGenre);
        edtDirector = findViewById(R.id.edtDirector);
        edtYear = findViewById(R.id.edtYear);
        edtRating = findViewById(R.id.edtRating);
        edtPoster = findViewById(R.id.edtPoster);
        edtDescription = findViewById(R.id.edtDescription);
        btnSaveMovie = findViewById(R.id.btnSaveMovie);
        btnBack = findViewById(R.id.btnBack);

        database = MovieDatabase.getDatabase(this);

        btnBack.setOnClickListener(v -> finish());

        // Check if editing an existing movie
        movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId != -1) {
            database.movieDao().getMovieById(movieId).observe(this, movie -> {
                if (movie != null) {
                    currentMovie = movie;
                    edtTitle.setText(movie.title);
                    edtGenre.setText(movie.genre);
                    edtDirector.setText(movie.director);
                    edtYear.setText(String.valueOf(movie.year));
                    edtRating.setText(String.valueOf(movie.rating));
                    edtPoster.setText(movie.poster);
                    edtDescription.setText(movie.description);
                    btnSaveMovie.setText("CẬP NHẬT PHIM");
                }
            });
        }

        btnSaveMovie.setOnClickListener(v -> saveMovie());
    }

    private void saveMovie() {
        String title = edtTitle.getText().toString().trim();
        String genre = edtGenre.getText().toString().trim();
        String director = edtDirector.getText().toString().trim();
        String yearStr = edtYear.getText().toString().trim();
        String ratingStr = edtRating.getText().toString().trim();
        String poster = edtPoster.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (title.isEmpty() || genre.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên phim, Thể loại và Năm phát hành", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Năm phát hành phải là số hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        float finalRating = 0.0f;
        if (!ratingStr.isEmpty()) {
            try {
                finalRating = Float.parseFloat(ratingStr);
                if (finalRating < 0 || finalRating > 10) {
                    Toast.makeText(this, "Điểm đánh giá từ 0 đến 10", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Điểm đánh giá phải là số hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        float ratingVal = finalRating;
        executor.execute(() -> {
            if (movieId == -1) {
                // Insert new movie
                MovieEntity movie = new MovieEntity();
                movie.title = title;
                movie.genre = genre;
                movie.director = director;
                movie.year = year;
                movie.rating = ratingVal;
                movie.poster = poster;
                movie.description = description;
                movie.favorite = false;
                movie.watched = false;

                database.movieDao().insertMovie(movie);
                runOnUiThread(() -> {
                    Toast.makeText(AddMovieActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                // Update existing movie
                if (currentMovie != null) {
                    currentMovie.title = title;
                    currentMovie.genre = genre;
                    currentMovie.director = director;
                    currentMovie.year = year;
                    currentMovie.rating = ratingVal;
                    currentMovie.poster = poster;
                    currentMovie.description = description;

                    database.movieDao().updateMovie(currentMovie);
                    runOnUiThread(() -> {
                        Toast.makeText(AddMovieActivity.this, "Cập nhật phim thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            }
            executor.shutdown();
        });
    }
}
