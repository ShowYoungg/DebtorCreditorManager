package com.example.debtorcreditormanager;

import android.app.Application;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    public static ProgressBar progressBar;
    private ImageView floatingActionImage;
    private UserViewModel mViewModel;
    private ProductAdapter productAdapter;
    private List<CustomerRecord> list;
    private List<CustomerRecord> recentlist;
    private List<Customer> customerlist;
    private Application application = getApplication();
    public static int monthDisbursements;
    public static int recentRepayment;
    public static int moneyInBusiness;
    public static int moneyDue;

    public static ProgressBar getProgressBar(){
        return progressBar;
    }

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

                progressBar.setVisibility(View.VISIBLE);
                mViewModel.getUsersDataFromCloud();
                mViewModel.getTransactionFromCloud();
                mViewModel.getLocalCount();
                Handler h = new Handler();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //The number of transaction records on cloud is compared against that of the local
                        //if local = cloud, the transactions are up to date;
                        //else if local is greater than cloud, it means there are pending transctions yet to be moved to cloud; hence update cloud;
                        //else, delete local and download from cloud to update local. Also customers' details will be deleted from local and be updated from cloud
                        int[] countDifference = mViewModel.updateTransactionInCloud();

                        if (countDifference[1] == -1) {
                            Toast.makeText(MainActivity.this, "Connect to the internet", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            return;
                        }

                        Log.i("SIZET", countDifference[0] + "/" + countDifference[1]);

                        if (countDifference[0] == countDifference[1]) {
                            Toast.makeText(MainActivity.this, "Transaction is up to date", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);

                        } else if (countDifference[0] > countDifference[1]) {
                            //Because there are transactions yet to be uploaded to cloud, upload those now.
                            Log.i("SIZET", countDifference[0] + "/" + countDifference[1]);

                            mViewModel.getTransactionById(countDifference[1]).observe(MainActivity.this, customerL -> {
                                Toast.makeText(MainActivity.this, "Uploading to cloud", Toast.LENGTH_SHORT).show();

                                String json = new Gson().toJson(customerL);
                                mViewModel.insertTransactionToCloudInBatches(json);
                            });

                            //This is to update the user data in the cloud.
                            String detailJson = new Gson().toJson(list);
                            mViewModel.uploadUserDataToCloud(detailJson);

                        } else {
                            Toast.makeText(MainActivity.this, staticTransactionList.size() + "size" + staticCustomerList.size(), Toast.LENGTH_SHORT).show();
                            //All transactions are deleted from local and re inserted from output from cloud
                            mViewModel.deleteAllTransactions();
                            for (Customer c : staticTransactionList) {
                                mViewModel.insertTrasanctionLocally(c);
                            }

                            //This will download customers data from cloud and insert into local database
                            for (CustomerRecord cr : staticCustomerList) {
                                //Toast.makeText(MainActivity.this, "Welcome from cloud " + cr.getDisbursement(), Toast.LENGTH_SHORT).show();
                                mViewModel.insertNewUserLocally(cr);
                            }
                        }
                    }
                }, 10000); //Wait for ten seconds

                break;

            case R.id.clear_data:
                //customDialog();
                customDialog();
                break;

            case R.id.disb:
                productAdapter.setCustomersList(list, "Disbursement");
                break;

            case R.id.bal:
                productAdapter.setCustomersList(list, "");
                break;

            case R.id.disb_month:
                productAdapter.setCustomersList(recentlist, "Disbursement");
                break;

            case R.id.analytics:
                startActivity(new Intent(this, DisbursementActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moneyInBusiness = 0;
        //mViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setEnabled(false);

        mViewModel = ViewModelProviders.of(this, new MyViewModelFactory(this.getApplication())).get(UserViewModel.class);

        recyclerView = findViewById(R.id.customer_list);
        progressBar.setVisibility(View.VISIBLE);

        floatingActionImage = findViewById(R.id.floating_action_image);

        productAdapter = new ProductAdapter(MainActivity.this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(productAdapter);

        //setUpViewModel();
        floatingActionImage.setOnClickListener(this);

        attachSwipeToDelete();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpViewModel();
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
                    updateDialog(accountNumber, list.get(position).getCustomerName(), list.get(position).getAddress());
                } else {
                    Toast.makeText(MainActivity.this, "The customer has not repaid balance due", Toast.LENGTH_SHORT).show();
                }
                productAdapter.notifyDataSetChanged();
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    public void updateDialog(int position, String name, String address) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle(getResources().getString(R.string.app_name))
                .setMessage("Do you want to update this account?")
                //.setMessage(getResources().getString(R.string.internet_error))
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getApplicationContext(), AddEntryActivity.class)
                                .putExtra("updatePosition", position)
                                .putExtra("customerName", name).putExtra("Address", address));
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                productAdapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    public void customDialog() {
        // custom dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_view);
        dialog.setCancelable(false);
        dialog.setTitle("Warning");

        EditText password = dialog.findViewById(R.id.password);
        Button cancel = dialog.findViewById(R.id.cancel);
        Button yes = dialog.findViewById(R.id.yes);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pass = password.getText().toString().trim();
                if (pass.equals("2464")) {
                    //progressBar.setVisibility(View.VISIBLE);
                    mViewModel.deleteAllCustomerData();
                    mViewModel.deleteAllTransactions();
                    list = new ArrayList<>();
                    productAdapter.setCustomersList(list, "");

                    dialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setUpViewModel() {
        monthDisbursements = 0;
        recentRepayment = 0;
        mViewModel.getCustomerList().observe(this, customerRecords -> {
            list = new ArrayList<>();
            list = customerRecords;
            progressBar.setVisibility(View.GONE);
            if (!list.isEmpty()) {
                productAdapter.setCustomersList(customerRecords, "");
            }

            //Gets recent month number; it will further be used to compare recent and past months information
            recentlist = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            String recentMonth = String.valueOf(Integer.valueOf(cal.get(Calendar.MONTH)) + 1);
            //String year = String.valueOf(cal.get(Calendar.YEAR));

            for (CustomerRecord cr : list) {

                //The below lines of code will sort customers whose payments are due and calculate moneyDue
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");

                    Date d1 = dateFormat.parse(cr.getDisbursementDate());
                    Date d2 = dateFormat.parse(dateFormat2.format(new Date()));

                    long difference = d2.getTime() - d1.getTime();
                    long dayDifference = TimeUnit.MILLISECONDS.toDays(difference);
                    Log.i("TIMEDIFFER", difference + "/" + dayDifference);
                    if (dayDifference >= 30 && Integer.valueOf(cr.getBalance()) > 0){
                        moneyDue += Integer.valueOf(cr.getBalance());
                    }

                } catch (ParseException pe){
                    pe.printStackTrace();
                }


                //The below lines of code sorts recent month and calculate recent information
                moneyInBusiness += Integer.valueOf(cr.getBalance());
                String m = cr.getDisbursementDate();
                String[] mm = m.split("/");
                if (mm.length == 1) {
                    Log.i("REASON", mm.length + "/" + m);
                    return;
                }


                String disbursementMonth = String.valueOf(Integer.valueOf(mm[1]));

                if (disbursementMonth.equals(recentMonth)) {
                    recentlist.add(cr);

                    monthDisbursements += Integer.valueOf(cr.getDisbursement());
                    recentRepayment += Integer.valueOf(cr.getDisbursement()) - Integer.valueOf(cr.getBalance());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        startActivity(new Intent(this, AddEntryActivity.class).putExtra("CustomerNumber", list.size()));
    }
}
