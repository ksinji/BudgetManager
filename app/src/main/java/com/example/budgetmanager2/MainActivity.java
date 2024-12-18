package com.example.budgetmanager2;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.budgetmanager2.DAO.BudgetDAO;
import com.example.budgetmanager2.DAO.ExpenseDAO;
import com.example.budgetmanager2.Database.AppDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static AppDatabase db;
    private RecyclerView recyclerView;
    private expenseAdapter adapter;
    ArrayList<Expense> expenseList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(this);


        // 현재 년도 및 월 나타내기
        TextView currentMonth = findViewById(R.id.currentMonth);

        Calendar calendar = Calendar.getInstance();
        String currentDateBig = new SimpleDateFormat("yyyy년 MM월", Locale.getDefault()).format(calendar.getTime());

        currentMonth.setText(currentDateBig);

        // 예산 정보 불러오기
        TextView budget = findViewById(R.id.budget);

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonthNum = calendar.get(Calendar.MONTH) + 1;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Budget budgetData = db.budgetDao().getBudgetByMonthYear(currentYear, currentMonthNum);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (budgetData != null) {
                            budget.setText(String.valueOf(budgetData.amount));
                        }
                    }
                });
            }
        }).start();

        // Budget Table 초기화
        insertInitialBudget(db.budgetDao());

        // 예산 변경 기능
        Button editBudget = findViewById(R.id.editBudget);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.edit_budget, null);
        builder.setView(dialogView);

        // dialogView에서 뷰 찾기
        final EditText input = dialogView.findViewById(R.id.profile_et);
        Button okButton = dialogView.findViewById(R.id.ok_btn);
        Button cancelButton = dialogView.findViewById(R.id.cancel_btn);

        editBudget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 매번 새로운 다이얼로그 뷰를 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.edit_budget, null);
                builder.setView(dialogView);

                // dialogView에서 뷰 찾기
                final EditText input = dialogView.findViewById(R.id.profile_et);
                Button okButton = dialogView.findViewById(R.id.ok_btn);
                Button cancelButton = dialogView.findViewById(R.id.cancel_btn);

                AlertDialog dialog = builder.create(); // 다이얼로그 생성
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // 투명 배경 설정

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int newBudget = Integer.parseInt(input.getText().toString());
                        updateBudget(newBudget);
                        budget.setText(input.getText().toString());
                        dialog.dismiss(); // 확인 버튼을 누르면 다이얼로그 사라짐
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel(); // 취소 버튼을 누르면 다이얼로그 사라짐
                    }
                });

                dialog.show(); // 다이얼로그 보여주기
            }
        });

        adapter = new expenseAdapter(expenseList, this);


        new Thread(new Runnable() {
            @Override
            public void run() {
                BudgetDAO budgetDao = AppDatabase.getDatabase(MainActivity.this).budgetDao();
                Integer budgetID = budgetDao.getIdByYearAndMonth(currentYear, currentMonthNum);
                Log.d("DB", "budgetID = " + budgetID);

                ExpenseDAO expenseDao = AppDatabase.getDatabase(MainActivity.this).expenseDao();
                List<Expense> expenses = expenseDao.getExpensesByBudgetId(budgetID);  // 현재 년도와 월에 맞는 Expense 객체만 불러오기
                Log.d("DB", "Expenses: " + expenses);

                expenseList.addAll(expenses);

                // Expense 테이블의 행 개수가 11개 미만이라면, 나머지를 새로운 Expense 객체로 채우기
                for(int i = expenses.size(); i < 11; i++) {
                    Expense newExpense = new Expense();
                    newExpense.setId(i+1);  // i를 id로 설정
                    newExpense.setBudgetId(budgetID); // BudgetId로 설정
                    expenseList.add(newExpense);
                }
            }
        }).start();


        // BudgetAdapter에게 데이터가 변경되었음을 알림
        adapter = new expenseAdapter(expenseList, this);
        adapter.notifyDataSetChanged();



        // RecyclerView와 Adapter 연결
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new expenseAdapter(expenseList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // + 버튼 클릭 시 행 추가
        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 새로운 Expense 객체를 생성하고 리스트에 추가
                Expense newExpense = new Expense();
                expenseList.add(newExpense);

                // 추가된 부분만 업데이트
                adapter.notifyItemInserted(expenseList.size() - 1);
            }
        });

        // 저장 버튼 클릭 시 Expense 테이블에 저장
        //TODO: 남은 예산 계산도 해줘야 함
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Expense> expenses = adapter.getExpenses(recyclerView);
                Log.d("MainActivity", "Expenses: " + expenses.toString()); // 반환하는 expenses 리스트를 로그로 출력

                ExpenseDAO expenseDao = AppDatabase.getDatabase(MainActivity.this).expenseDao();


                new Thread(() -> {  // 백그라운드 스레드에서 데이터베이스 작업 수행
                    for (Expense expense : expenses) {
                        BudgetDAO budgetDao = AppDatabase.getDatabase(MainActivity.this).budgetDao();
                        Integer budgetID = budgetDao.getIdByYearAndMonth(currentYear, currentMonthNum);
                        expense.setBudgetId(budgetID); // BudgetId로 설정
                        Log.d("MainActivity", "Expense: " + expense.toString()); // Expense 객체를 로그로 출력
                        Log.d("MainActivity", "Expense ID: " + expense.getId()); // Expense ID를 로그로 출력

                        long id = expenseDao.insert(expense); // insert 메소드를 사용하여 Expense 객체를 데이터베이스에 추가
                        Log.d("DB", "Inserted or updated expense with ID " + id);
                    }
                    // 데이터베이스 작업이 끝난 후에 남은 예산을 계산하고 보여주기
                    calculateAndShowRemainingBudget();
                }).start();  // 백그라운드 스레드 시작
            }
        });


        // 남은 예산 출력 기능
        // 새로운 스레드를 생성해서 DB 작업을 수행
        new Thread(new Runnable() {
            private int sumOfCost;
            @Override
            public void run() {
                // DB에서 해당 월의 예산 정보를 불러오기
                Budget budgetData = db.budgetDao().getBudgetByMonthYear(currentYear, currentMonthNum);
                // DB에서 해당 예산의 소비 정보의 합계를 불러오기 (null이 아닐 경우에만)
                if (budgetData != null) {
                    sumOfCost = db.expenseDao().getSumOfCost(budgetData.id);
                }

                // 메인 스레드에서 UI를 업데이트
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (budgetData != null) {
                            int remainingBudget = budgetData.amount - sumOfCost; // 남은 예산을 계산

                            // 남은 예산을 표시
                            TextView nowBudgetTextView = findViewById(R.id.nowBudget);
                            nowBudgetTextView.setText(String.valueOf(remainingBudget));
                        }
                    }
                });
            }
        }).start();
    }

    // RecyclerView Adapter와 ViewHolder
    public static class expenseAdapter extends RecyclerView.Adapter<expenseAdapter.ExpenseViewHolder> {
        private List<Expense> expenseList;
        private Context context;


        public expenseAdapter(List<Expense> expenseList, Context context) {
            this.expenseList = expenseList;
            this.context = context;
        }


        @NonNull
        @Override
        public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_recycler, parent, false);

            return new ExpenseViewHolder(view);
        }

        // Expense 객체 가져와 화면에 표시
        @Override
        public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
            Expense expense = expenseList.get(position);
            holder.id = expense.id;  // ViewHolder에 id 정보를 설정
            Log.d("DB", "expense.id = " + expense.id);
            String date = expense.getDate();
            String content = expense.getContent();
            String cost = expense.getCost();

            holder.date.setText(date);
            holder.content.setText(content);
            holder.cost.setText(String.valueOf(cost));
        }

        @Override
        public int getItemCount() {
            return expenseList.size();
        }


        public class ExpenseViewHolder extends RecyclerView.ViewHolder {

            public int id;
            public int budgetId;
            TextView date;
            EditText content;
            EditText cost;


            public ExpenseViewHolder(View itemView) {
                super(itemView);
                date = itemView.findViewById(R.id.selected_date);
                content = itemView.findViewById(R.id.content);
                cost = itemView.findViewById(R.id.cost);

                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDatePickerDialog((TextView) v, 1);
                    }
                });
            }

            // 선택된 날짜 출력 기능
            public void showDatePickerDialog(TextView v, int selectedRow) {

                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // 현재 년도 및 월의 첫 날과 마지막 날 계산
                Calendar firstDayOfMonth = Calendar.getInstance();
                firstDayOfMonth.set(year, month, 1);
                Calendar lastDayOfMonth = Calendar.getInstance();
                lastDayOfMonth.set(year, month, firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                Calendar selectedCalendar = Calendar.getInstance();
                                selectedCalendar.set(year, month, dayOfMonth);
                                int selectedDayOfWeek = selectedCalendar.get(Calendar.DAY_OF_WEEK);

                                // 요일을 문자열로 변환
                                String dayOfWeekStr;
                                switch (selectedDayOfWeek) {
                                    case Calendar.MONDAY:
                                        dayOfWeekStr = "월";
                                        break;
                                    case Calendar.TUESDAY:
                                        dayOfWeekStr = "화";
                                        break;
                                    case Calendar.WEDNESDAY:
                                        dayOfWeekStr = "수";
                                        break;
                                    case Calendar.THURSDAY:
                                        dayOfWeekStr = "목";
                                        break;
                                    case Calendar.FRIDAY:
                                        dayOfWeekStr = "금";
                                        break;
                                    case Calendar.SATURDAY:
                                        dayOfWeekStr = "토";
                                        break;
                                    case Calendar.SUNDAY:
                                        dayOfWeekStr = "일";
                                        break;
                                    default:
                                        dayOfWeekStr = "";
                                        break;
                                }

                                // 날짜 출력
                                String date = String.format("%02d/%02d(%s)", month + 1, dayOfMonth, dayOfWeekStr);
                                v.setText(date);

                            }
                        }, year, month, day);

                // DatePickerDialog에서 선택 가능한 날짜 범위 설정
                datePickerDialog.getDatePicker().setMinDate(firstDayOfMonth.getTimeInMillis());
                datePickerDialog.getDatePicker().setMaxDate(lastDayOfMonth.getTimeInMillis());

                datePickerDialog.show();
            }

        }


        public ArrayList<Expense> getExpenses(RecyclerView recyclerView) {
            ArrayList<Expense> expenses = new ArrayList<>();
            for (int i = 0; i < getItemCount(); i++) {
                ExpenseViewHolder holder = (ExpenseViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
                if (holder != null) {
                    Expense expense = new Expense();
                    if (holder.id != -1) {
                        expense.setId(holder.id);  // ViewHolder에 저장된 id 정보를 사용
                    }
                    // 날짜 문자열을 직접 설정
                    expense.setBudgetId(holder.budgetId);  // ViewHolder에 저장된 budgetId 정보를 사용
                    expense.setDate(holder.date.getText().toString());
                    expense.setContent(holder.content.getText().toString());
                    expense.setCost(holder.cost.getText().toString());
                    expenses.add(expense);
                } else {
                    Log.d("MainActivity", "ViewHolder for position " + i + " is null");
                }
            }
            return expenses;
        }
    }



    // Budget Table 초기화
    public void insertInitialBudget(BudgetDAO budgetDao) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;


        new Thread(new Runnable() {
            @Override
            public void run() {
                // 현재 년도와 월에 해당하는 Budget이 이미 존재하는지 확인
                Budget existingBudget = budgetDao.getBudgetByMonthYear(currentYear, currentMonth);
                if (existingBudget == null) {
                    // Budget이 존재하지 않는 경우에만 새로운 Budget을 삽입
                    Budget initialBudget = new Budget();
                    initialBudget.year = currentYear;
                    initialBudget.month = currentMonth;
                    initialBudget.amount = 0;

                    Log.d("Budget", "Year: " + initialBudget.year + ", Month: " + initialBudget.month + ", Amount: " + initialBudget.amount);
                    budgetDao.insert(initialBudget);
                }
            }
        }).start();
    }

    // 예산 DB 업데이트 메소드
    public void updateBudget(int newBudgetAmount){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                // 현재 년도 및 월을 가져옴
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Java의 Calendar에서 월은 0부터 시작하기 때문에 1을 더해줌

                // 현재 년도 및 월에 해당하는 예산 정보를 가져옴
                Budget budget = db.budgetDao().getBudgetByMonthYear(currentYear, currentMonth);

                if(budget != null){
                    // 해당 예산 정보가 있으면 업데이트
                    budget.amount = newBudgetAmount;
                    db.budgetDao().update(budget);
                } else {
                    // 해당 예산 정보가 없으면 새로 생성
                    Budget newBudget = new Budget();
                    newBudget.year = currentYear;
                    newBudget.month = currentMonth;
                    newBudget.amount = newBudgetAmount;
                    db.budgetDao().insert(newBudget);
                }
                } catch (Exception e) {
                    Log.e("BudgetUpdateError", "예산 업데이트 중 오류 발생", e);
                }
            }
        }).start();
    }


    // 남은 예산을 계산하고 보여주는 메소드
    private void calculateAndShowRemainingBudget() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonthNum = calendar.get(Calendar.MONTH) + 1;
        new Thread(new Runnable() {
            private int sumOfCost;
            @Override
            public void run() {
                Budget budgetData = db.budgetDao().getBudgetByMonthYear(currentYear, currentMonthNum);
                if (budgetData != null) {
                    sumOfCost = db.expenseDao().getSumOfCost(budgetData.id);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (budgetData != null) {
                            int remainingBudget = budgetData.amount - sumOfCost;
                            TextView nowBudgetTextView = findViewById(R.id.nowBudget);
                            nowBudgetTextView.setText(String.valueOf(remainingBudget));
                        }
                    }
                });
            }
        }).start();
    }

}
