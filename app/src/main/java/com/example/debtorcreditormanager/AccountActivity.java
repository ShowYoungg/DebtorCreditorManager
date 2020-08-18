package com.example.debtorcreditormanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {

    private CustomerRecord customerRecord;
    private CustomerRecord customerRecord2;
    private List<Customer> customerList;
    private Button addPayment;
    private UserViewModel mViewModel;
    private RecyclerView recyclerView;
    private EditText repayment;
    private AccountAdapter accountAdapter;
    private String confirmationMessage;
    private int bal;
    private TextView address, disbursement, balance, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        addPayment = findViewById(R.id.add_transaction);
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        repayment = findViewById(R.id.repayment);
        disbursement = findViewById(R.id.disbursement);
        balance = findViewById(R.id.balance);
        recyclerView = findViewById(R.id.account_list);

        accountAdapter = new AccountAdapter(AccountActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(accountAdapter);


        Intent intent = getIntent();
        if (intent != null) {
            customerRecord = intent.getParcelableExtra("CustomerRecord");
            customerRecord2 = customerRecord;
        }

        balance.setText("BAL: #" + customerRecord.getBalance());
        disbursement.setText("DISB: #" + customerRecord.getDisbursement());
        name.setText(customerRecord.getCustomerName());
        address.setText(customerRecord.getAddress());

        addPayment.setOnClickListener(this);
        setUpViewModel(customerRecord.getCustomerName());
    }


    private void setUpViewModel(String name) {
        mViewModel.getCustomerTransaction(name).observe(this, customerList1 -> {
        accountAdapter.setAccountList(customerList1);
        });
    }


    @Override
    public void onClick(View v) {

        if (repayment.getText().toString().isEmpty() || repayment.getText().toString().equals("") ||
                repayment.getText().toString().startsWith("0")){
            Toast.makeText(this, "Input today's repayment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.valueOf(customerRecord.getBalance()) < Integer.valueOf(repayment.getText().toString().trim())){
            Toast.makeText(this, "Input amount less or equal to balance", Toast.LENGTH_LONG).show();
            return;
        }

        Customer customer = new Customer();
        customer.setAccountNumber(customerRecord.getAccountNumber());
        customer.setCustomerName(customerRecord.getCustomerName());
        bal = Integer.valueOf(customerRecord.getBalance()) - Integer.valueOf(repayment.getText().toString().trim());
        customer.setDisbursement(customerRecord.getDisbursement());
        customer.setDisbursementDate(customerRecord.getDisbursementDate());
        customer.setPayback(repayment.getText().toString().trim());

        customer.setBalance(String.valueOf(bal));

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String s = df.format(new Date()).toString();

        customer.setDate(s);

        confirmationMessage = "Confirm the below information before continuing \n" + "Account Name: " + customer.getCustomerName() + "\n" +
        "Account Number: " + customer.getAccountNumber() + "\n" + "Today's repayment: #" + repayment.getText().toString() + "\n" +
                "Balance: #" + bal;
        confirmationDialog(confirmationMessage, customer, bal);
    }


    public void confirmationDialog(String message, Customer customer, int bal) {
        new AlertDialog.Builder(AccountActivity.this)
                .setTitle(getResources().getString(R.string.confirm_transaction))
                .setMessage(message)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Insert transaction locally
                        mViewModel.insertTrasanctionLocally(customer);
                        //Update user details locally e.g balance and new lastDate
                        mViewModel.updateUserLocally(String.valueOf(customerRecord.getAccountNumber()), customerRecord.getCustomerName(),
                                customer.getDate(), customerRecord.getAddress(), customerRecord.getDisbursement(),
                                customerRecord.getDisbursementDate(), String.valueOf(bal));

                        if (isOnline()){
                            //Insert transaction to cloud
                            mViewModel.insertTransactionToCloud(customer);

                            //Update user details on cloud e.g balance and new lastDate
                            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                            String s = df.format(new Date()).toString();

                            customerRecord.setLastDate(s);
                            customerRecord.setBalance(customer.getBalance());
                            mViewModel.updateUserInCloud(customerRecord);
                        }
                        finish();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }


    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

}
