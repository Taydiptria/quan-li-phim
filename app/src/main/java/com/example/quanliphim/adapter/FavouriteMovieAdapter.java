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

public class FavouriteMovieAdapter extends RecyclerView.Adapter<FavouriteMovieAdapter.ViewHolder> {

    private List<MovieEntity> movies = new ArrayList<>();
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(MovieEntity movie);
    }

    public FavouriteMovieAdapter(OnMovieClickListener listener) {
        this.listener = listener;
    }

    public void setMovies(List<MovieEntity> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_movie, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MovieEntity movie = movies.get(position);

        holder.tvTitle.setText(movie.title);

        if (movie.poster != null && !movie.poster.isEmpty()) {
            holder.imgPoster.setImageURI(Uri.parse(movie.poster));
        } else {
            holder.imgPoster.setImageResource(
                    android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMovieClick(movie);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPoster;
        TextView tvTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPoster = itemView.findViewById(R.id.imgFavoritePoster);
            tvTitle = itemView.findViewById(R.id.tvFavoriteTitle);
        }
    }
}
