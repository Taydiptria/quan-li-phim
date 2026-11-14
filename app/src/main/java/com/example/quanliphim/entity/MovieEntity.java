package com.example.quanliphim.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "movies")
public class MovieEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String genre;
    public String director;
    public int year;
    public String description;
    public String poster;
    public boolean favorite;
    public boolean watched;
    public float rating;

}
