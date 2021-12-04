package com.example.movielist;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.example.movielist.database.Movie;
import com.example.movielist.database.MovieDatabase;
import com.example.movielist.databinding.ItemLayoutBinding;

import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder> {

    protected MovieDatabase movieDatabase;



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,
                parent, false);
        return new ViewHolder(v);
    }

    private List<Movie> data;

    public ViewAdapter(List<Movie> data){
        this.data = data;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.movieTitleTextView.setText(data.get(position).getTitle());
        ImageView imageView = holder.binding.moviePosterImageView;
        Glide.with(holder.itemView).load("https://image.tmdb.org/t/p/w500" + data.get(position).getImage_path()).into(imageView);
        Log.d("BIND", "onBindViewHolder: " + data.get(position).isWatched());
        if(data.get(position).isWatched()){
            holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#8FF1C0"));
        }
        else
            holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public Movie getItemAtPosition (int position) {
        return data.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private MovieDatabase movieDatabase;
        ItemLayoutBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            movieDatabase = Room.databaseBuilder(
                    itemView.getContext(),
                    MovieDatabase.class,
                    "movie-db")
                    .fallbackToDestructiveMigration()
                    .build();

            binding = ItemLayoutBinding.bind(itemView);

            binding.getRoot().setOnClickListener(view -> {
                int mPosition = getLayoutPosition();

                Log.d("Onclick", "onBindViewHolder: " + data.get(mPosition).isWatched());
                if(!data.get(mPosition).isWatched()){
                    data.get(mPosition).setWatched(true);
                    binding.cardView.setCardBackgroundColor(Color.parseColor("#8FF1C0"));
                    new Thread(()->{
                        movieDatabase.movieDAO().setWatched(true, data.get(mPosition).getTitle());
                    }).start();
                }
                else{
                    data.get(mPosition).setWatched(false);
                    binding.cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
                    new Thread(()->{
                        movieDatabase.movieDAO().setWatched(false, data.get(mPosition).getTitle());
                    }).start();
                }
                ViewAdapter.this.notifyDataSetChanged();
            });
        }
    }
}
