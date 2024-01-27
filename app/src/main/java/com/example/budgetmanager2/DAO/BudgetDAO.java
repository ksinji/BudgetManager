package com.example.budgetmanager2.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.budgetmanager2.Budget;

import java.util.List;

@Dao
public interface BudgetDAO {
    @Insert
    void insert(Budget budget);

    @Insert
    long insertAndGetId(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("SELECT * FROM Budget")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM Budget WHERE year = :year AND month = :month LIMIT 1")
    Budget getBudgetByMonthYear(int year, int month);

    // 현재 년도 및 월에 맞는 id 가져오기 위한 쿼리
    @Query("SELECT id FROM Budget WHERE year = :year AND month = :month LIMIT 1")
    Integer getIdByYearAndMonth(int year, int month);
}