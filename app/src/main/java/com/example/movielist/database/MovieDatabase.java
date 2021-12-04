package com.example.movielist.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Movie.class}, version = 2)
public abstract class MovieDatabase extends RoomDatabase {
    public abstract MovieDAO movieDAO();
}
