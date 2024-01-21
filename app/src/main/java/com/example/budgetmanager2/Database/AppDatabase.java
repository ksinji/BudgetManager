package com.example.budgetmanager2.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.budgetmanager2.Budget;
import com.example.budgetmanager2.DAO.BudgetDAO;
import com.example.budgetmanager2.DAO.ExpenseDAO;
import com.example.budgetmanager2.Expense;

@Database(entities = {Budget.class, Expense.class}, version = 8, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BudgetDAO budgetDao();
    public abstract ExpenseDAO expenseDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "budget_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}