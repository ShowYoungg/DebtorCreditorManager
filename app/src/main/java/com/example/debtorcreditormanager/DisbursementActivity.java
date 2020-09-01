package com.example.debtorcreditormanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.debtorcreditormanager.MainActivity.moneyDue;
import static com.example.debtorcreditormanager.MainActivity.moneyInBusiness;
import static com.example.debtorcreditormanager.MainActivity.monthDisbursements;
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

        double roi = Double.valueOf((monthDisbursements * 0.1) / moneyInBusiness);
        analysis.setText("Business Analytics" + "\n\n" +
                "Investment: " + moneyInBusiness + "\n" +
                "Recent Disbursements: " + monthDisbursements + "\n" +
                "Recent Payment: " + recentRepayment + "\n" +
                "ROI without Penalty: " + Math.round(roi * 100.0) / 100.0 + "\n\n" +
                "Amount due: " + moneyDue + "\n" +
                "Loss on amount due: " + moneyDue / 10
        );

        summaryAdapter = new SummaryAdapter(DisbursementActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(summaryAdapter);

        loadTransactions();
    }

    /*
     * This method loads all transactions from database and sorts out today's disbursements and repayments
     * @ params: name
     * */
    private void loadTransactions() {
        mViewModel.getTransactionList().observe(this, customerList1 -> {
            HashMap<String, String[]> set = new HashMap<>();

            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String recentMonth = String.valueOf(Integer.valueOf(cal.get(Calendar.MONTH)) + 1);


            for (int i = 0; i < customerList1.size(); i++) {

                int dayPayment = 0;
                int dayDisbursement = 0;
                String m = customerList1.get(i).getDate();
                String[] ss = m.split("/");
                String month = ss[1];

                if (month.equals("0" + recentMonth)) {
                    if (set.containsKey(customerList1.get(i).getDate())) {
                        String[] d = set.get(customerList1.get(i).getDate());
                        if (d != null) {
                            dayPayment = Integer.valueOf(d[1]);
                            dayDisbursement = Integer.valueOf(d[2]);
                        }
                    }
                    //Identify each day disbursements
                    if (!customerList1.get(i).getPayback().equals("0")) {
                        //todayDisbursements += Integer.valueOf(c.getDisbursement());

                        dayPayment += Integer.valueOf(customerList1.get(i).getPayback());
                        set.put(customerList1.get(i).getDate(), new String[]{customerList1.get(i).getDate(), String.valueOf(dayPayment), String.valueOf(dayDisbursement)});
                        //Log.i("DISBURSEMENT", " " + todayDisbursements);
                    }
                    //Identify each day repayments
                    if (customerList1.get(i).getPayback().equals("0")) {
                        //todayRepayments += Integer.valueOf(c.getPayback());
                        dayDisbursement += Integer.valueOf(customerList1.get(i).getDisbursement());
                        set.put(customerList1.get(i).getDate(), new String[]{customerList1.get(i).getDate(), String.valueOf(dayPayment), String.valueOf(dayDisbursement)});
                        //Log.i("PAYMENT", " " + todayRepayments);
                    }
                }
            }
            summaryList.addAll(set.values());
            summaryAdapter.setTodaySummary(summaryList);
        });
    }
}
