package com.example.debtorcreditormanager;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "customer")
public class Customer implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int accountNumber;
    private String customerName;
    private String date;
    private String payback;
    private String disbursement;
    private String balance;
    private String disbursementDate;

    @Ignore
    public Customer(){
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.date = date;
        this.payback = payback;
        this.disbursement = disbursement;
        this.disbursementDate = disbursementDate;
        this.balance = balance;
    }

    public Customer(int id, int accountNumber, String customerName, String date,
                    String payback, String disbursement, String balance, String disbursementDate){

        this.id = id;
        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.date = date;
        this.payback = payback;
        this.disbursement = disbursement;
        this.disbursementDate = disbursementDate;
        this.balance = balance;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPayback() {
        return payback;
    }

    public void setPayback(String payback) {
        this.payback = payback;
    }

    public String getDisbursement() {
        return disbursement;
    }

    public void setDisbursement(String disbursement) {
        this.disbursement = disbursement;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getDisbursementDate() {
        return disbursementDate;
    }

    public void setDisbursementDate(String disbursementDate) {
        this.disbursementDate = disbursementDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeInt(this.accountNumber);
        dest.writeString(this.customerName);
        dest.writeString(this.date);
        dest.writeString(this.payback);
        dest.writeString(this.disbursement);
        dest.writeString(this.balance);
        dest.writeString(this.disbursementDate);
    }

    protected Customer(Parcel in) {
        this.id = in.readInt();
        this.accountNumber = in.readInt();
        this.customerName = in.readString();
        this.date = in.readString();
        this.payback = in.readString();
        this.disbursement = in.readString();
        this.balance = in.readString();
        this.disbursementDate = in.readString();
    }

    public static final Parcelable.Creator<Customer> CREATOR = new Parcelable.Creator<Customer>() {
        @Override
        public Customer createFromParcel(Parcel source) {
            return new Customer(source);
        }

        @Override
        public Customer[] newArray(int size) {
            return new Customer[size];
        }
    };
}
