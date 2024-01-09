package com.example.budgetmanager2.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.budgetmanager2.Expense;

import java.util.List;

@Dao
public interface ExpenseDAO {
    @Insert
    void insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    // 나중에 쓸 거!
    @Query("SELECT * FROM Expense WHERE budgetId = :budgetId")
    List<Expense> getExpensesForBudget(int budgetId);

    // id에 해당하는 Expense를 가져오는 메소드
    @Query("SELECT * FROM Expense WHERE id = :id")
    Expense getExpenseById(int id);

    // table 내용 전체 반환
    @Query("SELECT * FROM Expense")
    List<Expense> getAllExpenses();

    // 비용 합계 계산 --> 남은 예산 표시용
    @Query("SELECT SUM(cost) FROM expense WHERE budgetId = :budgetId")
    int getSumOfCost(int budgetId);

}