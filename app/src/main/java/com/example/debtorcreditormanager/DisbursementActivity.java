package com.example.debtorcreditormanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.debtorcreditormanager.MainActivity.moneyDue;
import static com.example.debtorcreditormanager.MainActivity.moneyInBusiness;
import static com.example.debtorcreditormanager.MainActivity.recentDisbursement;
import static com.example.debtorcreditormanager.MainActivity.recentRepayment;

public class DisbursementActivity extends AppCompatActivity {

    private TextView analysis;
    private RecyclerView recyclerView;
    private SummaryAdapter summaryAdapter;
    private UserViewModel mViewModel;
    private int todayDisbursements;
    private int todayRepayments;
    private List<String[]> summaryList;
    private SharedPreferences sharedPreferences;
    public static final String JSON_KEY = "JSON_OBJECT_CONVERTED_TO_STRING";
    public static final String LAST_DATE_KEY = "LAST_DAY";
    private String summaryListJson;
    private String lastDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disbursement);

        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        summaryList = new ArrayList<>();

        todayRepayments = 0;
        todayDisbursements = 0;

        recyclerView = findViewById(R.id.account_summary_list);
        analysis = findViewById(R.id.money_in_business);

        double d = Double.valueOf((recentDisbursement * 0.1) / moneyInBusiness);
        analysis.setText("Business Analytics" + "\n\n" +
                "Investment: " + moneyInBusiness + "\n" +
                "Recent Disbursements: " + recentDisbursement + "\n" +
                "Recent Payment: " + recentRepayment + "\n" +
                "ROI without Penalty: " + Math.round(d * 100.0) / 100.0 + "\n\n" +
                "Amount due: " + moneyDue + "\n" +
                "Loss on amount due: " + moneyDue / 10
        );

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = df.format(new Date()).toString();

        if (sharedPreferences != null) {
            summaryListJson = sharedPreferences.getString(JSON_KEY, "");
            lastDate = sharedPreferences.getString(LAST_DATE_KEY, "");
        }

        if (summaryListJson != null && summaryListJson.equals("")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<int[]>>() {
            }.getType();
            summaryList = gson.fromJson(summaryListJson, type);
        }

        summaryAdapter = new SummaryAdapter(DisbursementActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(summaryAdapter);

        loadTransactions(dateString);
    }

    /*
     * This method loads all transactions from database and sorts out today's disbursements and repayments
     * @ params: name
     * */
    private void loadTransactions(String s) {
        mViewModel.getTodaysTransactions(s).observe(this, customerList1 -> {
            summaryList = new ArrayList<>();
            HashMap<String, String[]> set = new HashMap<>();
            for (Customer c : customerList1) {
                if (c.getDate().equals(s)) {
                    //Identify today's disbursements
                    if (c.getPayback().equals("0")) {

                        todayDisbursements += Integer.valueOf(c.getDisbursement());
                        Log.i("DISBURSEMENT"," " + todayDisbursements);
                    }
                    //Identify today's repayments
                    if (!c.getPayback().equals("0")) {
                        todayRepayments += Integer.valueOf(c.getPayback());
                        Log.i("PAYMENT"," " + todayRepayments);
                    }
                    set.put(s, new String[]{s,String.valueOf(todayDisbursements), String.valueOf(todayRepayments)});
                    //summaryList.add(new String[]{s,String.valueOf(todayDisbursements), String.valueOf(todayRepayments)});
                }
            }
            summaryList.addAll(set.values());
            summaryAdapter.setTodaySummary(summaryList);
        });
        Gson gson = new Gson();
        String json = gson.toJson(summaryList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(JSON_KEY, json);
        editor.putString(LAST_DATE_KEY, s);
        editor.apply();
    }
}
