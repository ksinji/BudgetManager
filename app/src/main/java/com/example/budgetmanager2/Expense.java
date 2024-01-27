package com.example.budgetmanager2;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(foreignKeys = @ForeignKey(entity = Budget.class,
        parentColumns = "id",
        childColumns = "budgetId",
        onDelete = ForeignKey.CASCADE),
        indices = {@Index("budgetId")})
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int budgetId;

    public String date;

    public String content;

    public String cost = ""; // null 값 저장하기 위해 int -> Integer로 변경

    // ID를 설정하는 메소드
    public void setId(int id) { this.id = id; }

    public void setDate(String date) { this.date = date; }

    // 내용을 설정하는 메소드
    public void setContent(String content) {
        this.content = content;
    }

    // 비용을 설정하는 메소드
    public void setCost(String cost) {
        this.cost = cost;
    }

    // BudgetID를 설정하는 메소드
    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }


    public int getId() { return this.id;  }
    public String getDate() {
        return this.date;
    }
    public String getContent() {
        return this.content;
    }
    public String getCost() { return this.cost;  }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", budgetId=" + budgetId + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                ", cost=" + cost +
                '}';
    }
}