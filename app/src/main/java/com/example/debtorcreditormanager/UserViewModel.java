package com.example.debtorcreditormanager;

import android.app.Application;
import android.widget.Toast;

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


//    public void insertTransaction(Customer customer) {
//        mRepository.insertUserLocally(customer);
//    }

    public void insertNewUserToCloud(CustomerRecord customerRecord) {
        Toast.makeText( getApplication().getApplicationContext(), "going 2", Toast.LENGTH_SHORT).show();
        mRepository.insertNewUserToCloud(customerRecord);
    }

    public void insertNewUserLocally(CustomerRecord customerRecord) {
        mRepository.insertNewUserLocally(customerRecord);
    }

    public void insertTrasanctionLocally(Customer customer) {
        mRepository.insertTransactionLocally(customer);
    }

    public void insertTransactionToCloud(Customer customer) {
        mRepository.insertTransactionToCloud(customer);
    }

    public void deleteUser(String phone_number) {
        mRepository.deleteUser(phone_number);
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

    public LiveData<List<Customer>> getCustomerTransaction(String accountName) {
        return mRepository.getCustomerTransaction(accountName);
    }


    public void updateUserInCloud(CustomerRecord customerRecord) {
        mRepository.updateUserInCloud(customerRecord);
    }

    public void updateUserLocally(String accountNumber, String customerName,String lastDate, String address, String disbursement,String disbursementDate, String balance) {
        mRepository.updateUserLocally(accountNumber, customerName, lastDate, address, disbursement, disbursementDate, balance);
    }


}