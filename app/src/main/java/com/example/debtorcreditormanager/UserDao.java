package com.example.debtorcreditormanager;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(Customer customer);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNewUser(CustomerRecord customerRecord);


    @Query("SELECT * FROM CustomerList ORDER BY accountNumber")
    LiveData<List<CustomerRecord>> getAllCustomers();

    @Query("SELECT * FROM customer ORDER BY accountNumber")
    LiveData<List<Customer>> getAllTransaction();

    @Query("SELECT * FROM customer WHERE customerName =:accountName")
    LiveData<List<Customer>> getCustomerTransaction(String accountName);

    @Query("SELECT * FROM customer WHERE id >=:id")
    LiveData<List<Customer>> getTransactionById(int id);


    @Query("UPDATE CustomerList SET customerName =:customerName, lastDate = :lastDate, address = :address, disbursement =:disbursement,disbursementDate = :disbursementDate, balance =:balance " +
            " WHERE accountNumber =:accountNumber")
    void updateUser(String accountNumber, String customerName,String lastDate, String address, String disbursement,String disbursementDate, String balance);

    @Query("SELECT count(*) FROM customer")
    int getTransactionCount();

    @Query("DELETE FROM CustomerList WHERE accountNumber =:accountNumber")
    void deleteUser(String accountNumber);

    @Query("DELETE FROM customer")
    void deleteAllTransaction();

//    @Query("DELETE * FROM customer")
//    void deleteAll();

}