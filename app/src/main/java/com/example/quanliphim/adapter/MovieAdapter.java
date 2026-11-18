package com.example.quanliphim.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanliphim.R;
import com.example.quanliphim.entity.MovieEntity;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<MovieEntity> movieList = new ArrayList<>();
    private OnMovieClickListener listener;
    private OnFavoriteClickListener favoriteListener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieEntity movie);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(MovieEntity movie);
    }

    public MovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    public void setMovieList(List<MovieEntity> movies) {
        this.movieList = movies != null ? movies : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);

        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MovieViewHolder holder, int position) {

        MovieEntity movie = movieList.get(position);

        holder.tvTitle.setText(movie.title != null ? movie.title : "");
        holder.tvGenre.setText(movie.genre != null ? movie.genre : "");
        holder.tvYear.setText(String.valueOf(movie.year));

        if (movie.poster != null && !movie.poster.trim().isEmpty()) {
            try {
                holder.imgPoster.setImageURI(Uri.parse(movie.poster));
            } catch (Exception e) {
                holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgPoster.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Hiển thị trạng thái yêu thích
        if (movie.favorite) {
            holder.btnFavorite.setText("♥");
            holder.btnFavorite.setTextColor(
                    android.graphics.Color.parseColor("#FFC107"));
        } else {
            holder.btnFavorite.setText("♡");
            holder.btnFavorite.setTextColor(
                    android.graphics.Color.LTGRAY);
        }

        // Nhấn poster hoặc thông tin để mở chi tiết
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });

        // Nhấn trái tim để thêm hoặc bỏ yêu thích
        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null) {
                favoriteListener.onFavoriteClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView tvTitle, tvGenre, tvYear, btnFavorite;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            tvYear = itemView.findViewById(R.id.tvYear);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
