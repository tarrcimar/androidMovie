package com.example.movielist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.movielist.database.Movie;
import com.example.movielist.database.MovieDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PopActivity extends Activity {

    private static final String OMDB_KEY = "239cf86d8506f3dbfedbdbb08c02d26b";
    private static final String OMDB_ENDPOINT = "https://api.themoviedb.org/3/search/movie?api_key=" +
                                                OMDB_KEY + "&";

    private Button searchButton;
    private TextView textView;
    private EditText editText;
    private String title;
    private int releaseDate;
    private String description;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pop_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width *.7), (int)(height*.6));

        textView = findViewById(R.id.textView2);

        searchButton = findViewById(R.id.searchButton);
        editText = findViewById(R.id.editTextMovieTitle);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = String.format("https://api.themoviedb.org/3/search/movie?api_key=a1b83de092b31b002b1878923d9f143f&query=%1$s", editText.getText());
                List<String> jsonResponses = new ArrayList<>();

                RequestQueue requestQueue = Volley.newRequestQueue(PopActivity.this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("results");
                            for(int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.d("ORIGINAL", jsonObject.getString("original_title") + ": " + editText.getText());
                                if(jsonObject.getString("original_title").equals(editText.getText().toString())){
                                    title = jsonObject.getString("original_title");
                                    releaseDate = Integer.parseInt(jsonObject.getString("release_date").split("-")[0]);
                                    description = jsonObject.getString("overview");
                                    imagePath = jsonObject.getString("poster_path");

                                    new Thread(()-> {
                                        MovieDatabase db = Room.databaseBuilder(getApplicationContext(),
                                                MovieDatabase.class, "movie-db").allowMainThreadQueries().build();
                                        Movie movie = new Movie();
                                        movie.setTitle(title);
                                        movie.setDescription(description);
                                        movie.setImage_path(imagePath);
                                        movie.setReleaseDate(releaseDate);
                                        db.movieDAO().insertMovie(movie);

                                        Log.d("FASZ", db.movieDAO().getAllTitles().toString());
                                        Intent intent = new Intent();
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }).start();
                                    break;
                                }
                            }
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            textView.setText("Movie not found!");
                        }
                    }
                }, error -> error.printStackTrace());

                requestQueue.add(jsonObjectRequest);
            }
        });




    }
}
