
package com.example.quanliphim.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.quanliphim.dao.MovieDao;
import com.example.quanliphim.entity.MovieEntity;

@Database(
        entities = {MovieEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class MovieDatabase extends RoomDatabase {

    private static volatile MovieDatabase INSTANCE;

    public abstract MovieDao movieDao();

    public static MovieDatabase getDatabase(Context context) {

        if (INSTANCE == null) {
            synchronized (MovieDatabase.class) {

                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            MovieDatabase.class,
                            "movie_database"
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }

        return INSTANCE;
    }
}
