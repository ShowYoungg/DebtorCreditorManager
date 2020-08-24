package com.example.debtorcreditormanager;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private Repository mRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        mRepository = Repository.getInstance(application);
    }


    public void uploadUserDataToCloud(String json) {
        mRepository.updateUserDataInCloud(json);
//        for (CustomerRecord c: customerRecordList) {
//            Handler h = new Handler();
//            h.postAtTime(new Runnable() {
//                @Override
//                public void run() {
//                    mRepository.updateUserDataInCloud(c);
//                }
//            }, 1000);
//        }
    }

    public List<CustomerRecord> getUsersDataFromCloud(){
        return mRepository.getUsersDataFromCloud();
    }

    public List<Customer> getTransactionFromCloud() {
        return mRepository.getTransactionFromCloud();
    }




    public void insertNewUserToCloud(CustomerRecord customerRecord) {
        mRepository.insertNewUserToCloud(customerRecord);
    }

    public void insertNewUserLocally(CustomerRecord customerRecord) {
        mRepository.insertNewUserLocally(customerRecord);
    }

    public void insertTrasanctionLocally(Customer customer) {
        mRepository.insertTransactionLocally(customer);
    }

    public void insertTransactionToCloudInBatches(String json) {
        mRepository.insertTransactionToCloudInBatches(json);
    }

    public void insertTransactionToCloud(Customer customer) {
        mRepository.insertTransactionToCloud(customer);
    }

    public int[] updateTransactionInCloud() {
        return mRepository.updateTransactionInCloud();
    }


    public int getLocalCount() {
        return mRepository.getLocalCount();
    }

    public void deleteUserLocally(String accountNumber) {
        mRepository.deleteUserLocally(accountNumber);
    }


    public LiveData<List<CustomerRecord>> getCustomerList() {
        return mRepository.getCustomer();
    }

    public LiveData<List<Customer>> getTransactionList() {
        return mRepository.getTransaction();
    }

    public LiveData<List<Customer>> getTransactionById(int id) {
        return mRepository.getTransactionById(id);
    }


    public LiveData<List<Customer>> getCustomerTransaction(String accountName) {
        return mRepository.getCustomerTransaction(accountName);
    }

    public LiveData<List<Customer>> getTodaysTransactions(String date) {
        return mRepository.getTodaysTransactions(date);
    }



    public void updateUserInCloud(CustomerRecord customerRecord) {
        mRepository.updateUserInCloud(customerRecord);
    }

    public void updateUserLocally(String accountNumber, String customerName,String lastDate, String address, String disbursement,String disbursementDate, String balance) {
        mRepository.updateUserLocally(accountNumber, customerName, lastDate, address, disbursement, disbursementDate, balance);
    }

    public void deleteAllTransactions() {
        mRepository.deleteAllTransactionLocally();
    }

    public void deleteAllCustomerData() {
        mRepository.deleteAllCustomerDataLocally();
    }

}