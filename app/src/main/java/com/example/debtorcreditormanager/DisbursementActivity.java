package com.example.debtorcreditormanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.debtorcreditormanager.MainActivity.moneyDue;
import static com.example.debtorcreditormanager.MainActivity.moneyInBusiness;
import static com.example.debtorcreditormanager.MainActivity.recentDisbursement;
import static com.example.debtorcreditormanager.MainActivity.recentRepayment;

public class DisbursementActivity extends AppCompatActivity {

    private TextView analysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disbursement);

        analysis = findViewById(R.id.money_in_business);
        double d = Double.valueOf((recentDisbursement*0.1)/moneyInBusiness);
        analysis.setText("Business Analytics" + "\n\n" +
                "Investment: " + moneyInBusiness + "\n" +
                "Recent Disbursements: " + recentDisbursement + "\n" +
                "Recent Payment: " + recentRepayment + "\n" +
                "ROI without Penalty: " + Math.round(d * 100.0)/100.0 +"\n\n" +
                "Amount due: " + moneyDue + "\n" +
                "Loss on amount due: " + moneyDue/10
                );
    }
}
