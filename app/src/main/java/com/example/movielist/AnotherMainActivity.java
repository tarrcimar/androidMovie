package com.example.movielist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.request.transition.Transition;
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
        String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=a1b83de092b31b002b1878923d9f143f&query=%1$s",
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
