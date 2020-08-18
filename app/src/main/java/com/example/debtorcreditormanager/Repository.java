package com.example.debtorcreditormanager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {
    private static final String TAG = "UserRepository";
    private static Context mContext = null;
    private static Repository mRepository;
    private final String INSERT_URL_STRING = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec";
    UserListener mUserListener;
    UserDao mDao;


    public Repository(Context context) {
        mContext = context.getApplicationContext();
        mDao = UserDatabase.getInstance(context).mDao();
        getDailyData();
    }

    public static Repository getInstance(Context context) {
        if (mRepository == null) {
            mRepository = new Repository(context);
        }
        return mRepository;

    }


    private void getDailyData() {
        if (mUserListener != null) {
            mUserListener.onStarted();
        }

        GetDataService dataService = RetrofitInstance.getService();
        dataService.getResults().enqueue(new Callback<Info>() {
            @Override
            public void onResponse(Call<Info> call, Response<Info> response) {
                Info info = response.body();
                if (info != null && info.getItems() != null) {
                    if (mUserListener != null) {
                        mUserListener.onSuccess(info.getItems());
                        // mListMutableLiveData.setValue(info.getItems());
                    }
                    //System.out.println(info.getItems());
                }
            }

            @Override
            public void onFailure(Call<Info> call, Throwable t) {
                if (mUserListener != null) {
                    mUserListener.onFailed(t.getMessage());
                }
                t.printStackTrace();
            }
        });
    }

    private void getCustomerData() {
        if (mUserListener != null) {
            mUserListener.onStarted();
        }

        GetDataService dataService = RetrofitInstance.getService();
        dataService.getCustomerResults().enqueue(new Callback<Info>() {
            @Override
            public void onResponse(Call<Info> call, Response<Info> response) {
                Info info = response.body();
                if (info != null && info.getItems() != null) {
                    if (mUserListener != null) {
                        mUserListener.onSuccess(info.getItems());
                        // mListMutableLiveData.setValue(info.getItems());
                    }
                    //System.out.println(info.getItems());
                }
            }

            @Override
            public void onFailure(Call<Info> call, Throwable t) {
                if (mUserListener != null) {
                    mUserListener.onFailed(t.getMessage());
                }
                t.printStackTrace();
            }
        });

    }


    public void insertNewUserLocally(final CustomerRecord customerRecord) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDao.insertNewUser(customerRecord);
            }
        });

    }


    public void insertTransactionLocally(final Customer customer) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDao.insertTransaction(customer);
            }
        });

    }


    public void insertNewUserToCloud(CustomerRecord customerRecord) {
        Toast.makeText(mContext.getApplicationContext(), "going 1", Toast.LENGTH_SHORT).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR_RESPONSE", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "add_new_user");
                parmas.put("account_number", String.valueOf(customerRecord.getAccountNumber()));
                parmas.put("customer_name", customerRecord.getCustomerName());
                parmas.put("disbursement", customerRecord.getDisbursement());
                parmas.put("balance", customerRecord.getBalance());
                parmas.put("disbursement_date", customerRecord.getDisbursementDate());
                parmas.put("date", customerRecord.getLastDate());
                parmas.put("address", customerRecord.getAddress());

                return parmas;
            }
        };

        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(stringRequest);
    }


    public void insertTransactionToCloud(Customer customer) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR_RESPONSE", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "add_transaction");
                parmas.put("account_number", String.valueOf(customer.getAccountNumber()));
                parmas.put("customer_name", customer.getCustomerName());
                parmas.put("disbursement", customer.getDisbursement());
                parmas.put("balance", customer.getBalance());
                parmas.put("disbursement_date", customer.getDisbursementDate());
                parmas.put("date", customer.getDate());
                parmas.put("payback", customer.getPayback());

                return parmas;
            }
        };

        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(stringRequest);


    }

    public void updateUserInCloud(CustomerRecord customerRecord) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR_RESPONSE", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "update_user_details");
                parmas.put("account_number", String.valueOf(customerRecord.getAccountNumber()));
                parmas.put("customer_name", customerRecord.getCustomerName());
                parmas.put("disbursement", customerRecord.getDisbursement());
                parmas.put("balance", customerRecord.getBalance());
                parmas.put("disbursement_date", customerRecord.getDisbursementDate());
                parmas.put("date", customerRecord.getLastDate());
                parmas.put("address", customerRecord.getAddress());

                return parmas;
            }
        };

        int socketTimeOut = 50000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(stringRequest);
    }


    public void deleteUser(String accountNumber) {
        AppExecutors.getInstance().diskIO().execute(() -> mDao.deleteUser(accountNumber));
        GetDataService dataService = RetrofitInstance.getService();
        dataService.deleteUser(accountNumber).enqueue(new Callback<Customer>() {
            @Override
            public void onResponse(Call<Customer> call, Response<Customer> response) {
                //Utils.newInstance().toast(mContext, "delete succesfully!");
            }

            @Override
            public void onFailure(Call<Customer> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public void deleteUserLocally(String accountNumber) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDao.deleteUser(accountNumber);
            }
        });
    }


    public LiveData<List<CustomerRecord>> getCustomer() {
        return mDao.getAllCustomers();
    }

    public LiveData<List<Customer>> getTransaction() {
        return mDao.getAllTransaction();
    }

    public LiveData<List<Customer>> getCustomerTransaction(String accountName) {
        return mDao.getCustomerTransaction(accountName);
    }


    public void updateUserLocally(String accountNumber, String customerName, String lastDate, String address, String disbursement, String disbursementDate, String balance) {
        AppExecutors.getInstance().diskIO().execute(() -> mDao.updateUser(accountNumber, customerName, lastDate, address, disbursement, disbursementDate, balance));
    }

}
