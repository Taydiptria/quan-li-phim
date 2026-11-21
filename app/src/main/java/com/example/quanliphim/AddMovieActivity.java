package com.example.quanliphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddMovieActivity extends AppCompatActivity {

    EditText edtTitle, edtDirector, edtYear, edtRating, edtDescription, edtReview;
    Spinner spinnerGenre;
    ImageView imgPosterPreview;
    TextView tvPosterStatus, tvAddMovieHeader, btnBack;
    Button btnSelectPoster, btnRemovePoster, btnSaveMovie;

    MovieDatabase database;
    int movieId = -1;
    MovieEntity currentMovie;

    private String selectedPosterUri = "";

    private final String[] genreOptions = {
            "Hành động", "Tình cảm", "Hài hước", "Kinh dị",
            "Viễn tưởng", "Hoạt hình", "Tâm lý", "Phiêu lưu",
            "Võ thuật", "Tài liệu", "Gia đình", "Khác"
    };

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {}

                    selectedPosterUri = uri.toString();
                    displayPoster(selectedPosterUri);
                    Toast.makeText(AddMovieActivity.this, "Đã chọn ảnh poster thành công", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_add_movie);

        tvAddMovieHeader = findViewById(R.id.tvAddMovieHeader);
        edtTitle = findViewById(R.id.edtTitle);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        edtDirector = findViewById(R.id.edtDirector);
        edtYear = findViewById(R.id.edtYear);
        edtRating = findViewById(R.id.edtRating);
        edtDescription = findViewById(R.id.edtDescription);
        edtReview = findViewById(R.id.edtReview);

        imgPosterPreview = findViewById(R.id.imgPosterPreview);
        tvPosterStatus = findViewById(R.id.tvPosterStatus);
        btnSelectPoster = findViewById(R.id.btnSelectPoster);
        btnRemovePoster = findViewById(R.id.btnRemovePoster);

        btnSaveMovie = findViewById(R.id.btnSaveMovie);
        btnBack = findViewById(R.id.btnBack);

        setupGenreSpinner();

        database = MovieDatabase.getDatabase(this);

        btnBack.setOnClickListener(v -> finish());

        btnSelectPoster.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnRemovePoster.setOnClickListener(v -> {
            selectedPosterUri = "";
            displayPoster("");
        });

        movieId = getIntent().getIntExtra("movie_id", -1);
        if (movieId != -1) {
            tvAddMovieHeader.setText("CHỈNH SỬA PHIM");
            database.movieDao().getMovieById(movieId).observe(this, movie -> {
                if (movie != null) {
                    currentMovie = movie;
                    edtTitle.setText(movie.title);

                    selectGenreInSpinner(movie.genre);

                    edtDirector.setText(movie.director);
                    edtYear.setText(String.valueOf(movie.year));
                    edtRating.setText(String.valueOf(movie.rating));
                    edtDescription.setText(movie.description);
                    if (movie.review != null) {
                        edtReview.setText(movie.review);
                    }
                    selectedPosterUri = movie.poster != null ? movie.poster : "";
                    displayPoster(selectedPosterUri);
                    btnSaveMovie.setText("CẬP NHẬT PHIM");
                }
            });
        }

        btnSaveMovie.setOnClickListener(v -> saveMovie());
    }

    private void setupGenreSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.item_spinner_theme,
                genreOptions
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_theme);
        spinnerGenre.setAdapter(adapter);
    }

    private void selectGenreInSpinner(String genre) {
        if (genre == null) return;
        for (int i = 0; i < genreOptions.length; i++) {
            if (genreOptions[i].equalsIgnoreCase(genre.trim())) {
                spinnerGenre.setSelection(i);
                return;
            }
        }
    }

    private void displayPoster(String uriStr) {
        if (uriStr != null && !uriStr.trim().isEmpty()) {
            try {
                imgPosterPreview.setImageURI(Uri.parse(uriStr));
                tvPosterStatus.setText("Đã chọn ảnh từ thiết bị");
                tvPosterStatus.setTextColor(0xFF4CAF50);
                btnRemovePoster.setVisibility(View.VISIBLE);
                return;
            } catch (Exception ignored) {}
        }

        imgPosterPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        tvPosterStatus.setText("Chưa chọn ảnh từ thiết bị");
        tvPosterStatus.setTextColor(0xFF888888);
        btnRemovePoster.setVisibility(View.GONE);
    }

    private void saveMovie() {
        String title = edtTitle.getText().toString().trim();
        String genre = spinnerGenre.getSelectedItem() != null ? spinnerGenre.getSelectedItem().toString() : "Khác";
        String director = edtDirector.getText().toString().trim();
        String yearStr = edtYear.getText().toString().trim();
        String ratingStr = edtRating.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String review = edtReview.getText().toString().trim();

        if (title.isEmpty() || yearStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên phim và Năm phát hành", Toast.LENGTH_SHORT).show();
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
        String posterVal = selectedPosterUri;

        executor.execute(() -> {
            if (movieId == -1) {
                MovieEntity movie = new MovieEntity();
                movie.title = title;
                movie.genre = genre;
                movie.director = director;
                movie.year = year;
                movie.rating = ratingVal;
                movie.poster = posterVal;
                movie.description = description;
                movie.review = review;
                movie.favorite = false;
                movie.watched = false;

                database.movieDao().insertMovie(movie);
                runOnUiThread(() -> {
                    Toast.makeText(AddMovieActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                if (currentMovie != null) {
                    currentMovie.title = title;
                    currentMovie.genre = genre;
                    currentMovie.director = director;
                    currentMovie.year = year;
                    currentMovie.rating = ratingVal;
                    currentMovie.poster = posterVal;
                    currentMovie.description = description;
                    currentMovie.review = review;

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
