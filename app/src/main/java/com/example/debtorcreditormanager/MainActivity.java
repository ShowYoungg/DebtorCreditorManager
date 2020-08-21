package com.example.debtorcreditormanager;

import android.app.Application;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.debtorcreditormanager.Repository.staticCustomerList;
import static com.example.debtorcreditormanager.Repository.staticTransactionList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private ImageView floatingActionImage;
    private UserViewModel mViewModel;
    private ProductAdapter productAdapter;
    private List<CustomerRecord> list;
    private List<Customer> customerlist;
    private Application application = getApplication();


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_options, menu);

        //Associate searchable configuration with the SearchView
        SearchView searchView = (SearchView) menu.findItem(R.id.search_bar).getActionView();

        searchView.setQueryHint("Search by name or number");
        searchView.setSubmitButtonEnabled(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                productAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_cloud:

                mViewModel.getUsersDataFromCloud();
                mViewModel.getTransactionFromCloud();
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //The number of transaction records on cloud is compared against that of the local
                        //if local = cloud, the transactions are up to date;
                        //else if local is greater than cloud, it means there are pending transctions yet to be moved to cloud; hence update cloud;
                        //else, delete local and download from cloud to update local. Also customers' details will be deleted from local and be updated from cloud
                        int[] countDifference = mViewModel.updateTransactionInCloud();
                        if (countDifference[0] == countDifference[1]){
                            Toast.makeText(MainActivity.this, "Transaction is up to date", Toast.LENGTH_SHORT).show();
                        } else if (countDifference[0] > countDifference[1]){
                            //Because there are transactions yet to be uploaded to cloud, upload those now.
                            mViewModel.getTransactionById(countDifference[1] + 1).observe(MainActivity.this, customerL -> {
                                Toast.makeText(MainActivity.this, "Uploading to cloud", Toast.LENGTH_SHORT).show();

                                String json = new Gson().toJson(customerL);
                                mViewModel.insertTransactionToCloudInBatches(json);

                            });

                            //This is to update the user data in the cloud.
                            //This for loop will ensure retries for 5 more times
                            mViewModel.uploadUserDataToCloud(list);
                            for (int i = 0; i < 5; i++){
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewModel.uploadUserDataToCloud(list);
                                    }
                                }, 5000);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, staticTransactionList.size() + "size" + staticCustomerList.size(), Toast.LENGTH_SHORT).show();
                            //All transactions are deleted from local and re inserted from output from cloud
                            mViewModel.deleteAllTransactions();
                            for (Customer c : staticTransactionList) {
                                mViewModel.insertTrasanctionLocally(c);
                            }

                            //This will download customers data from cloud and insert into local database
                            for (CustomerRecord cr: staticCustomerList) {
                                Toast.makeText(MainActivity.this, "Welcome from cloud " + cr.getDisbursement(), Toast.LENGTH_SHORT).show();
                                mViewModel.insertNewUserLocally(cr);
                            }
                        }
                    }
                }, 10000); //Wait for ten seconds

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
//        mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        //mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        mViewModel = ViewModelProviders.of(this, new MyViewModelFactory(this.getApplication())).get(UserViewModel.class);
        //mViewModel= new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(this.getApplication())).get(UserViewModel.class);

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
                if (list.get(position).getBalance().equals(String.valueOf(0))) {
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
