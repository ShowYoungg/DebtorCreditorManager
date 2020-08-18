package com.example.debtorcreditormanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private ImageView floatingActionImage;
    private UserViewModel mViewModel;
    private ProductAdapter productAdapter;
    private List<CustomerRecord> list;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.sync:
                break;

            case R.id.disb:
                productAdapter.setCustomersList(list, "Disbursement");
                break;

            case R.id.bal:
                productAdapter.setCustomersList(list, "");
                break;

            case R.id.disb_month:
                productAdapter.setCustomersList(list, "Recent");
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = new ArrayList<>();
        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        recyclerView = findViewById(R.id.customer_list);
        floatingActionImage = findViewById(R.id.floating_action_image);

        productAdapter = new ProductAdapter(MainActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(productAdapter);

        setUpViewModel();
        floatingActionImage.setOnClickListener(this);

        attachSwipeToDelete();
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT | ItemTouchHelper.DOWN | ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //Toast.makeText(ListActivity.this, "on Move", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();
                int accountNumber = list.get(position).getAccountNumber();
                if (list.get(position).getBalance().equals(String.valueOf(0))){
                    updateDialog(accountNumber, list.get(position).getCustomerName());
                } else {
                    Toast.makeText(MainActivity.this, "The customer has not repaid balance due", Toast.LENGTH_SHORT).show();
                }
                productAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void updateDialog(int position, String name) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage("Do you want to update this account?")
                //.setMessage(getResources().getString(R.string.internet_error))
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), AddEntryActivity.class)
                                .putExtra("updatePosition", position).putExtra("customerName", name));
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                productAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    private void setUpViewModel() {
        mViewModel.getCustomerList().observe(this, customerRecords -> {
            list = customerRecords;
            productAdapter.setCustomersList(customerRecords, "");
        });
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, AddEntryActivity.class).putExtra("CustomerNumber", list.size()));
    }
}
