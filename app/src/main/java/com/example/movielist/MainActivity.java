package com.example.movielist;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.movielist.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    String[] items = new String[]{"1", "2", "3", "4"};
    //List<String> items;


    @Override
    protected void onResume() {
        super.onResume();
        new Thread(()-> {
            MovieDatabase db = Room.databaseBuilder(getApplicationContext(),
                    MovieDatabase.class, "movie-db").allowMainThreadQueries().build();

            runOnUiThread(() ->{
                items = new String[db.movieDAO().getAllTitles().size()];
                db.movieDAO().getAllTitles().toArray(items);
                binding.moviesListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items));
                binding.moviesListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Toast.makeText(MainActivity.this, items[i], Toast.LENGTH_SHORT).show();
                        MovieDetailsActivity movieDetailsActivity = new MovieDetailsActivity();
                        movieDetailsActivity.setTitle(items[i]);
                        Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                        intent.putExtra("title", items[i]);
                        startActivityForResult(intent, 1);
                    }
                });
            });
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setUpDatabase


        binding.floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PopActivity.class);
                //startActivity(new Intent(MainActivity.this, PopActivity.class));
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }
}