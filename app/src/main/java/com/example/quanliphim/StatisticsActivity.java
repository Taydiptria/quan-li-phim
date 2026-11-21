package com.example.quanliphim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanliphim.database.MovieDatabase;
import com.example.quanliphim.entity.MovieEntity;
import com.example.quanliphim.util.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    TextView btnBack;
    TextView tvStatTotalMovies, tvStatWatchedMovies, tvStatWatchedPercent;
    TextView tvStatFavoriteMovies, tvStatFavPercent, tvStatAvgRating;

    ProgressBar pbWatchedRatio;
    TextView tvWatchedRatioLabel, tvUnwatchedRatioLabel;

    LinearLayout containerGenreBars, containerRatingBars;
    TextView tvNoGenreData;

    MovieDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.applySavedTheme(this);
        setContentView(R.layout.activity_statistics);

        btnBack = findViewById(R.id.btnBack);
        tvStatTotalMovies = findViewById(R.id.tvStatTotalMovies);
        tvStatWatchedMovies = findViewById(R.id.tvStatWatchedMovies);
        tvStatWatchedPercent = findViewById(R.id.tvStatWatchedPercent);
        tvStatFavoriteMovies = findViewById(R.id.tvStatFavoriteMovies);
        tvStatFavPercent = findViewById(R.id.tvStatFavPercent);
        tvStatAvgRating = findViewById(R.id.tvStatAvgRating);

        pbWatchedRatio = findViewById(R.id.pbWatchedRatio);
        tvWatchedRatioLabel = findViewById(R.id.tvWatchedRatioLabel);
        tvUnwatchedRatioLabel = findViewById(R.id.tvUnwatchedRatioLabel);

        containerGenreBars = findViewById(R.id.containerGenreBars);
        containerRatingBars = findViewById(R.id.containerRatingBars);
        tvNoGenreData = findViewById(R.id.tvNoGenreData);

        btnBack.setOnClickListener(v -> finish());

        database = MovieDatabase.getDatabase(this);

        database.movieDao().getAllMovies().observe(this, this::calculateAndDisplayStatistics);
    }

    private void calculateAndDisplayStatistics(List<MovieEntity> movies) {
        if (movies == null || movies.isEmpty()) {
            displayEmptyStatistics();
            return;
        }

        int totalMovies = movies.size();
        int watchedCount = 0;
        int favoriteCount = 0;
        float totalRatingSum = 0;
        int ratedMoviesCount = 0;

        Map<String, Integer> genreMap = new HashMap<>();

        int score8To10 = 0;
        int score6To79 = 0;
        int score4To59 = 0;
        int scoreBelow4 = 0;

        for (MovieEntity m : movies) {
            if (m.watched) watchedCount++;
            if (m.favorite) favoriteCount++;

            if (m.rating > 0) {
                totalRatingSum += m.rating;
                ratedMoviesCount++;
            }

            String g = (m.genre == null || m.genre.trim().isEmpty()) ? "Khác" : m.genre.trim();
            genreMap.put(g, genreMap.getOrDefault(g, 0) + 1);

            if (m.rating >= 8.0f) {
                score8To10++;
            } else if (m.rating >= 6.0f) {
                score6To79++;
            } else if (m.rating >= 4.0f) {
                score4To59++;
            } else {
                scoreBelow4++;
            }
        }

        tvStatTotalMovies.setText(String.valueOf(totalMovies));
        tvStatWatchedMovies.setText(String.valueOf(watchedCount));

        int watchedPct = (int) Math.round((watchedCount * 100.0) / totalMovies);
        tvStatWatchedPercent.setText(watchedPct + "% tổng bộ sưu tập");

        tvStatFavoriteMovies.setText(String.valueOf(favoriteCount));
        int favPct = (int) Math.round((favoriteCount * 100.0) / totalMovies);
        tvStatFavPercent.setText(favPct + "% tổng bộ sưu tập");

        float avgRating = ratedMoviesCount > 0 ? (totalRatingSum / ratedMoviesCount) : 0.0f;
        tvStatAvgRating.setText(String.format(Locale.getDefault(), "%.1f", avgRating));

        pbWatchedRatio.setProgress(watchedPct);
        int unwatchedCount = totalMovies - watchedCount;
        int unwatchedPct = 100 - watchedPct;
        tvWatchedRatioLabel.setText(String.format(Locale.getDefault(), "Đã xem: %d (%d%%)", watchedCount, watchedPct));
        tvUnwatchedRatioLabel.setText(String.format(Locale.getDefault(), "Chưa xem: %d (%d%%)", unwatchedCount, unwatchedPct));

        containerGenreBars.removeAllViews();
        if (genreMap.isEmpty()) {
            tvNoGenreData.setVisibility(View.VISIBLE);
        } else {
            tvNoGenreData.setVisibility(View.GONE);

            List<Map.Entry<String, Integer>> genreList = new ArrayList<>(genreMap.entrySet());
            Collections.sort(genreList, (e1, e2) -> e2.getValue().compareTo(e1.getValue()));

            for (Map.Entry<String, Integer> entry : genreList) {
                String genreName = entry.getKey();
                int count = entry.getValue();
                int pct = (int) Math.round((count * 100.0) / totalMovies);

                addStatBarRow(containerGenreBars, genreName, count, pct, "#FFC107");
            }
        }

        containerRatingBars.removeAllViews();
        addStatBarRow(containerRatingBars, "8.0 - 10.0 (Xuất sắc)", score8To10, (int) Math.round((score8To10 * 100.0) / totalMovies), "#4CAF50");
        addStatBarRow(containerRatingBars, "6.0 - 7.9 (Khá / Tốt)", score6To79, (int) Math.round((score6To79 * 100.0) / totalMovies), "#2196F3");
        addStatBarRow(containerRatingBars, "4.0 - 5.9 (Trung bình)", score4To59, (int) Math.round((score4To59 * 100.0) / totalMovies), "#FF9800");
        addStatBarRow(containerRatingBars, "Dưới 4.0 (Cần cải thiện)", scoreBelow4, (int) Math.round((scoreBelow4 * 100.0) / totalMovies), "#F44336");
    }

    private void addStatBarRow(LinearLayout container, String labelText, int count, int percentage, String colorHex) {
        View rowView = LayoutInflater.from(this).inflate(R.layout.item_stat_bar, container, false);

        TextView tvBarLabel = rowView.findViewById(R.id.tvBarLabel);
        TextView tvBarValue = rowView.findViewById(R.id.tvBarValue);
        ProgressBar progressBar = rowView.findViewById(R.id.progressBar);

        tvBarLabel.setText(labelText);
        tvBarValue.setText(String.format(Locale.getDefault(), "%d phim (%d%%)", count, percentage));

        progressBar.setProgress(percentage);

        try {
            int color = android.graphics.Color.parseColor(colorHex);
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
        } catch (Exception ignored) {}

        container.addView(rowView);
    }

    private void displayEmptyStatistics() {
        tvStatTotalMovies.setText("0");
        tvStatWatchedMovies.setText("0");
        tvStatWatchedPercent.setText("0% tổng bộ sưu tập");
        tvStatFavoriteMovies.setText("0");
        tvStatFavPercent.setText("0% tổng bộ sưu tập");
        tvStatAvgRating.setText("0.0");

        pbWatchedRatio.setProgress(0);
        tvWatchedRatioLabel.setText("Đã xem: 0 (0%)");
        tvUnwatchedRatioLabel.setText("Chưa xem: 0 (0%)");

        containerGenreBars.removeAllViews();
        tvNoGenreData.setVisibility(View.VISIBLE);

        containerRatingBars.removeAllViews();
    }
}
