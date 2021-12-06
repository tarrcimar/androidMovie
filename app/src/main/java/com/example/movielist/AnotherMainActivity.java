package com.example.movielist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.movielist.database.Movie;
import com.example.movielist.database.MovieDatabase;
import com.example.movielist.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnotherMainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MovieDatabase movieDatabase;
    private String title;
    private int releaseDate;
    private String description;
    private String imagePath;

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.moviesRecyclerView.setLayoutManager(new LinearLayoutManager(AnotherMainActivity.this));

        //setUpDatabase
        movieDatabase = Room.databaseBuilder(
                this,
                MovieDatabase.class,
                "movie-db")
                .fallbackToDestructiveMigration()
                .build();

        movieDatabase.movieDAO().getAllTitles().observe(this,
                movieItems -> binding.moviesRecyclerView.setAdapter(new ViewAdapter(movieItems)));

        binding.floatingActionButton2.setOnClickListener(view -> addItem());
        binding.nuke.setOnClickListener(
                view -> new Thread(() -> movieDatabase.movieDAO().nuke()).start());

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN|ItemTouchHelper.UP, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                recyclerView.getAdapter().notifyDataSetChanged();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                final Movie myMovie =
                        ((ViewAdapter)binding.moviesRecyclerView.getAdapter()).getItemAtPosition(position);
                new Thread(() -> movieDatabase.movieDAO().deleteItem(myMovie)).start();
                binding.moviesRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                Toast.makeText(AnotherMainActivity.this, "Deleted " + myMovie.getTitle(), Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(binding.moviesRecyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN|ItemTouchHelper.UP, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                recyclerView.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                recyclerView.getAdapter().notifyDataSetChanged();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                final Movie myMovie =
                        ((ViewAdapter)binding.moviesRecyclerView.getAdapter()).getItemAtPosition(position);
                //new Thread(() -> movieDatabase.movieDAO().deleteItem(myMovie)).start();
                Intent intent = new Intent(AnotherMainActivity.this, MovieDetailsActivity.class);
                intent.putExtra("title", myMovie.getTitle());
                startActivity(intent);
                binding.moviesRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                Toast.makeText(AnotherMainActivity.this, myMovie.getTitle() + " opened.", Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(binding.moviesRecyclerView);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, AnotherMainActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }

    private void addItem() {
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=%1$s&query=%2$s",
                getString(R.string.tmd_api_key),
                binding.searchMovieEditText.getText());
        List<String> jsonResponses = new ArrayList<>();

        RequestQueue requestQueue = Volley.newRequestQueue(AnotherMainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("results");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getString("original_title").equals(binding.searchMovieEditText.getText().toString())) {
                            Log.d("RESPONSE", "onResponse: " + jsonArray);
                            title = jsonObject.getString("original_title");
                            releaseDate = Integer.parseInt(jsonObject.getString("release_date").split("-")[0]);
                            description = jsonObject.getString("overview");
                            imagePath = jsonObject.getString("poster_path");

                            new Thread(() -> {
                                Movie movie = new Movie();
                                movie.setTitle(title);
                                movie.setDescription(description);
                                movie.setImage_path(imagePath);
                                movie.setReleaseDate(releaseDate);
                                movie.setWatched(false);
                                Log.d("MOVIE", "onResponse: " + movie.toString());
                                movieDatabase.movieDAO().insertMovie(movie);
                            }).start();
                            binding.searchMovieEditText.setText(null);
                            Toast.makeText(AnotherMainActivity.this, title + " added.", Toast.LENGTH_SHORT);
                            break;
                        }
                    }
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Movie not found", Toast.LENGTH_SHORT).show();
                }
            }
        }, error -> error.printStackTrace());

        requestQueue.add(jsonObjectRequest);
    }
}
