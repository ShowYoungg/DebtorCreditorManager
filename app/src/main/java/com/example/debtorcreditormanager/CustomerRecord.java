package com.example.debtorcreditormanager;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "CustomerList")
public class CustomerRecord implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int accountNumber;
    private String customerName;
    private String disbursement;
    private String balance;
    private String disbursementDate;
    private String lastDate;
    private String address;


    @Ignore
    public CustomerRecord(){
        this.customerName = customerName;
        this.lastDate = lastDate;
        this.address = address;
        this.disbursement = disbursement;
        this.disbursementDate = disbursementDate;
        this.balance = balance;
    }

    public CustomerRecord(int accountNumber, String customerName, String lastDate,
                    String address, String disbursement, String balance, String disbursementDate){

        this.accountNumber = accountNumber;
        this.customerName = customerName;
        this.lastDate = lastDate;
        this.address = address;
        this.disbursement = disbursement;
        this.disbursementDate = disbursementDate;
        this.balance = balance;
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

    public String getLastDate() {
        return lastDate;
    }

    public void setLastDate(String lastDate) {
        this.lastDate = lastDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.accountNumber);
        dest.writeString(this.customerName);
        dest.writeString(this.disbursement);
        dest.writeString(this.balance);
        dest.writeString(this.disbursementDate);
        dest.writeString(this.lastDate);
        dest.writeString(this.address);
    }

    protected CustomerRecord(Parcel in) {
        this.accountNumber = in.readInt();
        this.customerName = in.readString();
        this.disbursement = in.readString();
        this.balance = in.readString();
        this.disbursementDate = in.readString();
        this.lastDate = in.readString();
        this.address = in.readString();
    }

    public static final Parcelable.Creator<CustomerRecord> CREATOR = new Parcelable.Creator<CustomerRecord>() {
        @Override
        public CustomerRecord createFromParcel(Parcel source) {
            return new CustomerRecord(source);
        }

        @Override
        public CustomerRecord[] newArray(int size) {
            return new CustomerRecord[size];
        }
    };
}
