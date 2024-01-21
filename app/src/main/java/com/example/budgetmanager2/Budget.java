package com.example.budgetmanager2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int year;

    public int month;

    public int amount;

}