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
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class Repository {
    private static final String TAG = "UserRepository";
    private static Context mContext = null;
    private static Repository mRepository;
    public static List<CustomerRecord> staticCustomerList;
    public static List<Customer> staticTransactionList;
    private LiveData<List<Customer>> customerList1;
    private final String INSERT_URL_STRING = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec";
    UserListener mUserListener;
    UserDao mDao;
    int cloudCount = 0;
    int localCount = 0;


    public Repository(Context context) {
        mContext = context.getApplicationContext();
        mDao = UserDatabase.getInstance(context).mDao();

        getTransactionFromCloud();
        getUsersDataFromCloud();
    }

    public static Repository getInstance(Context context) {
        if (mRepository == null) {
            mRepository = new Repository(context);
        }
        return mRepository;
    }


    public List<CustomerRecord> getUsersDataFromCloud() {
        String URL_BASE = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=read_user_details";

        //String json = downloadJSON(URL_BASE);
        return downloadJSON(URL_BASE);
    }

    private List<CustomerRecord> downloadJSON(String urll) {

        final List<CustomerRecord>[] l = new List[]{new ArrayList<>()};
        //final String[] s = new String[1];
        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(Void... voids) {

                String jsonResponse = "";
                try {
                    jsonResponse = getHTTPResponse(urll);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.i("JSonRESPONSE", jsonResponse);
                return jsonResponse;
            }

            @Override
            protected void onPostExecute(String jsonResponse) {
                super.onPostExecute(jsonResponse);
                //s[0] = jsonResponse;
                l[0] = parseCustomerJSON(jsonResponse);
                Log.i("JSONRESPONSE", jsonResponse);
            }
        }.execute();
        return l[0];
    }


    private List<CustomerRecord> parseCustomerJSON(String json) {
        staticCustomerList = new ArrayList<>();
        List<CustomerRecord> custList = new ArrayList<>();
        try {
            JSONObject mainJSONObject = new JSONObject(json);
            JSONArray itemsArray = mainJSONObject.getJSONArray("items");

            for (int i = 0; i < itemsArray.length(); i++) {
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
                staticCustomerList.add(customerRecord);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return custList;
    }

    private List<Customer> parseTransactionJSON(String urll) {
        List<Customer> custList = new ArrayList<>();

        new AsyncTask<Void, Void, String>(){

            @Override
            protected String doInBackground(Void... voids) {
                String jsonResponse = "";
                try {
                    jsonResponse = getHTTPResponse(urll);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Log.i("JSonRESPONSE", jsonResponse);
                return jsonResponse;

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                staticTransactionList = new ArrayList<>();
                try {
                    JSONObject mainJSONObject = new JSONObject(s);
                    JSONArray itemsArray = mainJSONObject.getJSONArray("items");

                    for (int i = 0; i < itemsArray.length(); i++) {
                        JSONObject childObject = itemsArray.getJSONObject(i);
                        String accountNumber = childObject.getString("AccountNumber");
                        String accountName = childObject.getString("AccountName");
                        String date = childObject.getString("Date");
                        String disbursement = childObject.getString("Disbursement");
                        String payment = childObject.getString("Payment");
                        String balance = childObject.getString("Balance");
                        String disbursementDate = childObject.getString("DisbursementDate");

                        Customer customer = new Customer();
                        customer.setAccountNumber(Integer.valueOf(accountNumber));
                        customer.setCustomerName(accountName);
                        customer.setDisbursement(disbursement);
                        customer.setBalance(balance);
                        customer.setDisbursementDate(disbursementDate);
                        customer.setDate(date);
                        customer.setPayback(payment);

                        //Toast.makeText(mContext, "Transaction from " + accountName, Toast.LENGTH_SHORT).show();

                        custList.add(customer);
                        staticTransactionList.add(customer);
                    }
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
        }.execute();
        return custList;
    }


    private String getHTTPResponse(String urll) throws IOException {
        URL url = null;
        try {
            url = new URL(urll);
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
        String URL_BASE = "https://script.google.com/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=read_user_transactions";
        //String transactionJson = downloadJSON(URL_BASE);
        return parseTransactionJSON(URL_BASE);
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

    public int[] updateTransactionInCloud(){

        cloudCount = staticTransactionList.size();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                localCount = mDao.getTransactionCount();
            }
        });

        Toast.makeText(mContext, staticCustomerList.size() + "%%%%" + cloudCount, Toast.LENGTH_SHORT).show();
        return new int[] {localCount, cloudCount};
    }

    public void insertTransactionToCloud(Customer customer) {
        //Toast.makeText(mContext, customerJson, Toast.LENGTH_LONG).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
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

    public void insertTransactionToCloudInBatches(String json) {
        //Toast.makeText(mContext, customerJson, Toast.LENGTH_LONG).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, INSERT_URL_STRING,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("ERRR", response);
                        Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
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
                parmas.put("action", "add_batch_transaction");
                parmas.put("jso", json);

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

    public LiveData<List<Customer>> getTransactionById(int id) {
        return mDao.getTransactionById(id);
    }


    public LiveData<List<Customer>> getCustomerTransaction(String accountName) {
        return mDao.getCustomerTransaction(accountName);
    }


    public void updateUserLocally(String accountNumber, String customerName, String lastDate, String address, String disbursement, String disbursementDate, String balance) {
        AppExecutors.getInstance().diskIO().execute(() -> mDao.updateUser(accountNumber, customerName, lastDate, address, disbursement, disbursementDate, balance));
    }

    public void updateUserDataInCloud(CustomerRecord customerRecord) {

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

    public void deleteAllTransactionLocally() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDao.deleteAllTransaction();
            }
        });

    }



}
