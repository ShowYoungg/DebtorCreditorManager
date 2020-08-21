package com.example.debtorcreditormanager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.lifecycle.ViewModelProviders;

public class AddEntryActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText name, address, amount;
    private Button register;
    private UserViewModel mViewModel;
    private String customerName;
    private int numberOfCustomer;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_entry);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        Intent intent = getIntent();

        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        amount = findViewById(R.id.disbursement);
        register = findViewById(R.id.register);

        if (intent != null){

            if (intent.hasExtra("CustomerNumber")){
                numberOfCustomer = intent.getIntExtra("CustomerNumber", -1);
                customerName = intent.getStringExtra("customerName");
            }

            if (intent.hasExtra("updatePosition")){
                numberOfCustomer = intent.getIntExtra("updatePosition", -2);
                customerName = intent.getStringExtra("customerName");
                register.setText("UPDATE");
            }
        }
        name.setText(customerName);
        register.setOnClickListener(this);
    }

    private void setUpViewModel(CustomerRecord customerRecord, Customer customer) {

        if (register.getText().equals("UPDATE")){
            mViewModel.updateUserLocally(String.valueOf(numberOfCustomer), customerRecord.getCustomerName(),
                    customerRecord.getLastDate(), customerRecord.getAddress(), customerRecord.getDisbursement(),
                    customerRecord.getDisbursementDate(), customerRecord.getBalance());

            mViewModel.insertTrasanctionLocally(customer);

            if (isOnline()){
                mViewModel.updateUserInCloud(customerRecord);
                mViewModel.insertTransactionToCloud(customer);
            }
        } else {
            mViewModel.insertNewUserLocally(customerRecord);
            mViewModel.insertTrasanctionLocally(customer);
            if (isOnline()){
                mViewModel.insertNewUserToCloud(customerRecord);
                mViewModel.insertTransactionToCloud(customer);
            }
        }
    }


    @Override
    public void onClick(View v) {
        String customerName = name.getText().toString().trim();
        String customerAddress = address.getText().toString().trim();
        String disbursement = amount.getText().toString().trim();

        if (disbursement.startsWith("0") || disbursement.equals("") || disbursement.isEmpty()){
            Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (customerName.equals("") || customerName.isEmpty()){
            Toast.makeText(this, "Invalid Name", Toast.LENGTH_SHORT).show();
            return;
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String s = df.format(new Date()).toString();

        String disbursementDate = s;
        String lastDate = disbursementDate;
        String balance = disbursement;

        CustomerRecord customerRecord = new CustomerRecord();
        if (getIntent().hasExtra("updatePosition")){
            customerRecord.setAccountNumber(numberOfCustomer);
        } else {
            customerRecord.setAccountNumber(numberOfCustomer + 1);
        }
        customerRecord.setCustomerName(customerName);
        customerRecord.setAddress(customerAddress);
        customerRecord.setDisbursement(disbursement);
        customerRecord.setDisbursementDate(disbursementDate);
        customerRecord.setLastDate(lastDate);
        customerRecord.setBalance(balance);

        Customer customer = new Customer();
        customer.setAccountNumber(customerRecord.getAccountNumber());
        customer.setCustomerName(customerName);
        customer.setDate(lastDate);
        customer.setPayback("0");
        customer.setDisbursement(disbursement);
        customer.setBalance(balance);
        customer.setDisbursementDate(disbursementDate);


        setUpViewModel(customerRecord, customer);
        //Toast.makeText(this, "Customer registered successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    public boolean checkInternetConnection() {
        boolean status = false;
        Socket sock = new Socket();
        InetSocketAddress address = new InetSocketAddress("www.google.com", 80);
        try {
            sock.connect(address, 3000);
            if (sock.isConnected()) status = true;
        } catch (Exception e) {
            status = false;
        } finally {
            try {
                sock.close();
            } catch (Exception e) {
            }
        }
        return status;
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
