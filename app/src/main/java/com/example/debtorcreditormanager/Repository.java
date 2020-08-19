package com.example.debtorcreditormanager;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import androidx.lifecycle.LiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Repository {
    private static final String TAG = "UserRepository";
    private static Context mContext = null;
    private static Repository mRepository;
    private List<CustomerRecord> customerRecordList;
    private final String INSERT_URL_STRING = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec";
    UserListener mUserListener;
    UserDao mDao;


    public Repository(Context context) {
        mContext = context.getApplicationContext();
        mDao = UserDatabase.getInstance(context).mDao();
        getTransactionFromCloud();
    }

    public static Repository getInstance(Context context) {
        if (mRepository == null) {
            mRepository = new Repository(context);
        }
        return mRepository;
    }


    public List<CustomerRecord> getUsersDataFromCloud() throws IOException {
        return downloadJSON();
    }

    private List<CustomerRecord> downloadJSON() {

        final List<CustomerRecord>[] l = new List[]{new ArrayList<>()};
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {

                String jsonResponse = "";
                try {
                    jsonResponse = getHTTPResponse();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.i("JSonRESPONSE", jsonResponse);
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(String jsonResponse) {
                super.onPostExecute(jsonResponse);
                l[0] = parseCustemrJSON(jsonResponse);
                Log.i("JSONRESPONSE", jsonResponse);
            }
        }.execute();
        return l[0];
    }


    private List<CustomerRecord> parseCustemrJSON(String json) {
        List<CustomerRecord> custList = new ArrayList<>();
        try {
            JSONObject mainJSONObject = new JSONObject(json);
            JSONArray itemsArray = mainJSONObject.getJSONArray("items");

//            for (Iterator<String> it = itemsArray.getJSONObject(0).keys(); it.hasNext(); ) {
//                String s = it.next();
//                Log.i("CHILDOBJECT", s);
//            }

            for (int i = 0; i < itemsArray.length(); i++){
                JSONObject childObject = itemsArray.getJSONObject(i);
                String accountNumber = childObject.getString("AccountNumber");
                String accountName = childObject.getString("AccountName");
                String disbursement = childObject.getString("Disbursement");
                String balance = childObject.getString("Balance");
                String disbursementDate = childObject.getString("DisbursementDate");
                String lastDate = childObject.getString("LastDate");
                String address = childObject.getString("Address");

                CustomerRecord customerRecord = new CustomerRecord();
                customerRecord.setAccountNumber(Integer.valueOf(accountNumber));
                customerRecord.setCustomerName(accountName);
                customerRecord.setDisbursement(disbursement);
                customerRecord.setBalance(balance);
                customerRecord.setDisbursementDate(disbursementDate);
                customerRecord.setLastDate(lastDate);
                customerRecord.setAddress(address);

                Toast.makeText(mContext, accountName, Toast.LENGTH_SHORT).show();

                custList.add(customerRecord);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return custList;
    }

    private String getHTTPResponse() throws IOException {
        String URL_BASE = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=read_user_details";
        URL url = null;
        try {
            url = new URL(URL_BASE);
        } catch (MalformedURLException exception) {
            Log.e("URL_CREATION", "Error creating URL", exception);
        }

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    public List<Customer> getTransactionFromCloud() {
        if (mUserListener != null) {
            mUserListener.onStarted();
        }
        final List<Customer>[] customerList = new List[]{new ArrayList<>()};

        GetDataService dataService = RetrofitBuilder.Retrieve();
        dataService.getTransactionFromCloud().enqueue(new Callback<List<Customer>>() {
            @Override
            public void onResponse(Call<List<Customer>> call, Response<List<Customer>> response) {
                customerList[0] = response.body();
            }

            @Override
            public void onFailure(Call<List<Customer>> call, Throwable t) {
                if (mUserListener != null) {
                    mUserListener.onFailed(t.getMessage());
                }
                t.printStackTrace();
            }
        });

        return customerList[0];
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
                        //Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
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
                        //Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
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


//    public void deleteUser(String accountNumber) {
//        AppExecutors.getInstance().diskIO().execute(() -> mDao.deleteUser(accountNumber));
//        GetDataService dataService = RetrofitBuilder.getService();
//        dataService.deleteUser(accountNumber).enqueue(new Callback<Customer>() {
//            @Override
//            public void onResponse(Call<Customer> call, Response<Customer> response) {
//                //Utils.newInstance().toast(mContext, "delete succesfully!");
//            }
//
//            @Override
//            public void onFailure(Call<Customer> call, Throwable t) {
//                t.printStackTrace();
//            }
//        });
//    }

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

    public void syncronizeNewUserWithCloud(CustomerRecord customerRecord) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        //Toast.makeText(mContext, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.i("ERROR_RESPONSE", error.getLocalizedMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                //here we pass params
                parmas.put("action", "sync");
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

        int socketTimeOut = 90000;// u can change this .. here it is 50000 = 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(stringRequest);


    }


}
