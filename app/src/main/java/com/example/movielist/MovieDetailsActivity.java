package com.example.movielist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

public class MovieDetailsActivity extends AppCompatActivity {

    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    private String date;

    public void setDate(String date) {
        this.date = date;
    }

    private ImageView imageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;
    private ImageView delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.movie_detail);
        Bundle extras = getIntent().getExtras();
        if(extras!=null) setTitle(extras.getString("title"));

        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        delete = findViewById(R.id.delete);

        new Thread(()-> {
            MovieDatabase db = Room.databaseBuilder(getApplicationContext(),
                    MovieDatabase.class, "movie-db").allowMainThreadQueries().build();

            Movie movie = db.movieDAO().findByTitle(title);

            runOnUiThread(() ->{
                Glide.with(this).load("https://image.tmdb.org/t/p/w500" + movie.getImage_path()).into(imageView);
                titleTextView.setTypeface(null, Typeface.BOLD);
                titleTextView.setText(movie.getTitle());
                dateTextView.setText(Integer.toString(movie.getReleaseDate()));
                setDate(Integer.toString(movie.getReleaseDate()));
                descriptionTextView.setMovementMethod(new ScrollingMovementMethod());
                descriptionTextView.setText(movie.getDescription());
            });
        }).start();

        imageView.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + title + "+" + date));
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        delete.setOnClickListener(view -> new Thread(()-> {
            MovieDatabase db = Room.databaseBuilder(getApplicationContext(),
                    MovieDatabase.class, "movie-db").allowMainThreadQueries().build();

            Intent refresh = new Intent(MovieDetailsActivity.this, AnotherMainActivity.class);
            startActivity(refresh);
            overridePendingTransition(android.R.anim.accelerate_interpolator, android.R.anim.decelerate_interpolator);
            MovieDetailsActivity.this.finish();
            db.movieDAO().deleteByTitle(title);
        }).start());

    }
}
