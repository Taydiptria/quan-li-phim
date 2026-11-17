package com.example.quanliphim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieDetailActivity extends AppCompatActivity {

    ImageView imgPoster;
    TextView tvTitle, tvGenre, tvYear, tvDirector, tvRating, tvDescription, tvDetailReview, tvWatchedBadge, tvFavoriteBadge, btnBack;
    Button btnEdit, btnDelete, btnRateReview, btnToggleWatched, btnToggleFavorite;

    MovieDatabase database;
    int movieId = -1;
    MovieEntity currentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_movie_detail);

        imgPoster = findViewById(R.id.imgDetailPoster);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvGenre = findViewById(R.id.tvDetailGenre);
        tvYear = findViewById(R.id.tvDetailYear);
        tvDirector = findViewById(R.id.tvDetailDirector);
        tvRating = findViewById(R.id.tvDetailRating);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvDetailReview = findViewById(R.id.tvDetailReview);
        tvWatchedBadge = findViewById(R.id.tvDetailWatchedBadge);
        tvFavoriteBadge = findViewById(R.id.tvDetailFavoriteBadge);
        btnBack = findViewById(R.id.btnBack);

        btnRateReview = findViewById(R.id.btnRateReview);
        btnToggleWatched = findViewById(R.id.btnToggleWatched);
        btnToggleFavorite = findViewById(R.id.btnToggleFavorite);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnBack.setOnClickListener(v -> finish());

        movieId = getIntent().getIntExtra("movie_id", -1);

        if (movieId == -1) {
            Toast.makeText(this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        database = MovieDatabase.getDatabase(this);

        database.movieDao().getMovieById(movieId).observe(this, movie -> {
            if (movie == null) {
                return;
            }
            currentMovie = movie;
            displayMovie(movie);
        });

        btnRateReview.setOnClickListener(v -> showRatingDialog());

        btnToggleWatched.setOnClickListener(v -> toggleWatchedState());

        btnToggleFavorite.setOnClickListener(v -> toggleFavoriteState());

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

    private void displayMovie(MovieEntity movie) {
        tvTitle.setText(movie.title);
        tvGenre.setText("Thể loại: " + movie.genre);
        tvYear.setText("Năm phát hành: " + movie.year);
        tvDirector.setText("Đạo diễn: " + (movie.director == null || movie.director.isEmpty() ? "Chưa cập nhật" : movie.director));

        tvRating.setText(String.format(Locale.getDefault(), "%.1f / 10", movie.rating));

        if (movie.review != null && !movie.review.trim().isEmpty()) {
            tvDetailReview.setText(movie.review);
        } else {
            tvDetailReview.setText("Chưa có nhận xét nào cho bộ phim này. Nhấn nút bên dưới để chấm điểm và viết nhận xét.");
        }

        tvDescription.setText(
                movie.description == null || movie.description.isEmpty()
                        ? "Chưa có mô tả cho bộ phim này."
                        : movie.description
        );

        if (movie.watched) {
            tvWatchedBadge.setVisibility(View.VISIBLE);
            btnToggleWatched.setText("✔ ĐÃ XEM");
        } else {
            tvWatchedBadge.setVisibility(View.GONE);
            btnToggleWatched.setText("+ ĐÁNH DẤU ĐÃ XEM");
        }

        if (movie.favorite) {
            tvFavoriteBadge.setVisibility(View.VISIBLE);
            btnToggleFavorite.setText("♥ YÊU THÍCH");
        } else {
            tvFavoriteBadge.setVisibility(View.GONE);
            btnToggleFavorite.setText("♡ THÊM YÊU THÍCH");
        }

        if (movie.poster != null && !movie.poster.isEmpty()) {
            try {
                imgPoster.setImageURI(Uri.parse(movie.poster));
            } catch (Exception e) {
                imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void showRatingDialog() {
        if (currentMovie == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating_review, null);

        TextView tvMovieNameHeader = dialogView.findViewById(R.id.tvMovieNameHeader);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        TextView tvScoreDisplay = dialogView.findViewById(R.id.tvScoreDisplay);
        EditText edtScoreValue = dialogView.findViewById(R.id.edtScoreValue);
        EditText edtReviewComment = dialogView.findViewById(R.id.edtReviewComment);
        CheckBox cbMarkWatched = dialogView.findViewById(R.id.cbMarkWatched);
        Button btnCancelRating = dialogView.findViewById(R.id.btnCancelRating);
        Button btnSaveRating = dialogView.findViewById(R.id.btnSaveRating);

        tvMovieNameHeader.setText(currentMovie.title);
        cbMarkWatched.setChecked(currentMovie.watched);

        float initialRating = currentMovie.rating;
        ratingBar.setRating(initialRating / 2.0f);
        tvScoreDisplay.setText(String.format(Locale.getDefault(), "%.1f / 10", initialRating));
        edtScoreValue.setText(String.format(Locale.getDefault(), "%.1f", initialRating));

        if (currentMovie.review != null) {
            edtReviewComment.setText(currentMovie.review);
        }

        final boolean[] isSelfUpdating = {false};

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser && !isSelfUpdating[0]) {
                isSelfUpdating[0] = true;
                float score10 = rating * 2.0f;
                tvScoreDisplay.setText(String.format(Locale.getDefault(), "%.1f / 10", score10));
                edtScoreValue.setText(String.format(Locale.getDefault(), "%.1f", score10));
                isSelfUpdating[0] = false;
            }
        });

        edtScoreValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isSelfUpdating[0]) {
                    try {
                        float val = Float.parseFloat(s.toString());
                        if (val >= 0 && val <= 10) {
                            isSelfUpdating[0] = true;
                            ratingBar.setRating(val / 2.0f);
                            tvScoreDisplay.setText(String.format(Locale.getDefault(), "%.1f / 10", val));
                            isSelfUpdating[0] = false;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnCancelRating.setOnClickListener(v -> dialog.dismiss());

        btnSaveRating.setOnClickListener(v -> {
            String scoreStr = edtScoreValue.getText().toString().trim();
            float newRating = currentMovie.rating;
            if (!scoreStr.isEmpty()) {
                try {
                    newRating = Float.parseFloat(scoreStr);
                    if (newRating < 0 || newRating > 10) {
                        Toast.makeText(MovieDetailActivity.this, "Điểm đánh giá phải từ 0 đến 10", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(MovieDetailActivity.this, "Điểm đánh giá không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String newReview = edtReviewComment.getText().toString().trim();
            boolean markWatched = cbMarkWatched.isChecked();

            currentMovie.rating = newRating;
            currentMovie.review = newReview;
            if (markWatched) {
                currentMovie.watched = true;
            }

            updateMovieInDatabase(currentMovie, "Đã lưu đánh giá và nhận xét thành công!");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void toggleWatchedState() {
        if (currentMovie == null) return;
        currentMovie.watched = !currentMovie.watched;
        String msg = currentMovie.watched ? "Đã đánh dấu là Đã xem" : "Đã chuyển thành Chưa xem";
        updateMovieInDatabase(currentMovie, msg);
    }

    private void toggleFavoriteState() {
        if (currentMovie == null) return;
        currentMovie.favorite = !currentMovie.favorite;
        String msg = currentMovie.favorite ? "Đã thêm vào danh sách Yêu thích" : "Đã xóa khỏi Yêu thích";
        updateMovieInDatabase(currentMovie, msg);
    }

    private void updateMovieInDatabase(MovieEntity movie, String successMessage) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            database.movieDao().updateMovie(movie);
            runOnUiThread(() -> Toast.makeText(MovieDetailActivity.this, successMessage, Toast.LENGTH_SHORT).show());
            executor.shutdown();
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
}
