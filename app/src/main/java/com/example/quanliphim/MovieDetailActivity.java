package com.example.quanliphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailActivity extends AppCompatActivity {

    ImageView imgPoster;
    TextView tvTitle, tvGenre, tvYear;
    TextView tvDirector, tvRating, tvDescription, btnBack;
    Button btnEdit, btnDelete;

    MovieDatabase database;
    int movieId = -1;
    MovieEntity currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        imgPoster = findViewById(R.id.imgDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvGenre = findViewById(R.id.tvDetailGenre);
        tvYear = findViewById(R.id.tvDetailYear);
        tvDirector = findViewById(R.id.tvDetailDirector);
        tvRating = findViewById(R.id.tvDetailRating);
        tvDescription = findViewById(R.id.tvDetailDescription);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnBack.setOnClickListener(v -> finish());

        movieId = getIntent().getIntExtra("movie_id", -1);

        if (movieId == -1) {
            Toast.makeText(this, "Không tìm thấy phim",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        database = MovieDatabase.getDatabase(this);

        database.movieDao().getMovieById(movieId)
                .observe(this, movie -> {

                    if (movie == null) {
                        Toast.makeText(this, "Phim không tồn tại",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    currentMovie = movie;
                    displayMovie(movie);
                });

        btnEdit.setOnClickListener(v -> {
            if (currentMovie != null) {
                Intent intent = new Intent(MovieDetailActivity.this, AddMovieActivity.class);
                intent.putExtra("movie_id", currentMovie.id);
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (currentMovie != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Xóa phim")
                        .setMessage("Bạn có chắc chắn muốn xóa phim \"" + currentMovie.title + "\"?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteMovie())
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
    }

    private void deleteMovie() {
        if (currentMovie == null) return;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            database.movieDao().deleteMovie(currentMovie);
            runOnUiThread(() -> {
                Toast.makeText(MovieDetailActivity.this, "Đã xóa phim thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
            executor.shutdown();
        });
    }

    private void displayMovie(MovieEntity movie) {

        tvTitle.setText(movie.title);
        tvGenre.setText("Thể loại: " + movie.genre);
        tvYear.setText("Năm phát hành: " + movie.year);
        tvDirector.setText("Đạo diễn: " + movie.director);

        tvRating.setText(String.format(
                Locale.getDefault(), "%.1f / 10", movie.rating));

        tvDescription.setText(
                movie.description == null || movie.description.isEmpty()
                        ? "Chưa có mô tả cho bộ phim này."
                        : movie.description
        );

        if (movie.poster != null && !movie.poster.isEmpty()) {
            imgPoster.setImageURI(Uri.parse(movie.poster));
        } else {
            imgPoster.setImageResource(
                    android.R.drawable.ic_menu_gallery);
        }
    }
}
