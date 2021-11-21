package com.example.movielist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.room.Room;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class MovieDetailsActivity extends Activity {

    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    private ImageView imageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.movie_detail);
        Bundle extras = getIntent().getExtras();
        if(extras!=null) setTitle(extras.getString("title"));

        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        new Thread(()-> {
            MovieDatabase db = Room.databaseBuilder(getApplicationContext(),
                    MovieDatabase.class, "movie-db").allowMainThreadQueries().build();

            Movie movie = db.movieDAO().findByTitle(title);

            runOnUiThread(() ->{
                Glide.with(this).load("https://image.tmdb.org/t/p/w500" + movie.getImage_path()).into(imageView);
                titleTextView.setTypeface(null, Typeface.BOLD);
                titleTextView.setText(movie.getTitle());
                dateTextView.setText(Integer.toString(movie.getReleaseDate()));
                descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                descriptionTextView.setText(movie.getDescription());
            });
        }).start();

    }
}
