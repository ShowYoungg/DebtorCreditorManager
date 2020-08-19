package com.example.debtorcreditormanager;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;


public interface GetDataService {

    @GET("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=read_user_details")
    Call<List<CustomerRecord>> getCustomerRecordsFromCloud();

    @GET("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=read_customer_details")
    Call<List<Customer>> getTransactionFromCloud();


    @POST("/macros/s/AKfycbxv2qHdE6-GAS-JqEKzAkIvRDetPNd6ua5wT2DmDBD7od8Kxhs/exec?action=add_new_user")
    //@POST("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=add_new_user")
    @FormUrlEncoded
    Call<CustomerRecord> saveNewUser(@Field("account_number") int accountNumber,
                               @Field("customer_name") String customerName,
                               @Field("disbursement") String disbursement,
                               @Field("balance") String balance,
                               @Field("disbursement_date") String disbursementDate,
                               @Field("date") String date,
                               @Field("address") String address);

    //@POST("/macros/s/AKfycbxv2qHdE6-GAS-JqEKzAkIvRDetPNd6ua5wT2DmDBD7od8Kxhs/exec?action=add_user_details")
    @POST("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=add_user_details")
    @FormUrlEncoded
    Call<Customer> saveUser(@Field("account_number") int accountNumber,
                            @Field("customer_name") String customerName,
                            @Field("date") String date,
                            @Field("disbursement") String disbursement,
                            @Field("payback") String payback,
                            @Field("balance") String balance,
                            @Field("disbursement_date") String disbursementDate);


    @POST("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=update_user_details")
    @FormUrlEncoded
    Call<Customer> updateUser(@Field("account_number") int accountNumber,
                              @Field("customer_name") String customerName,
                              @Field("date") String date,
                              @Field("disbursement") String disbursement,
                              @Field("payback") String payback,
                              @Field("balance") String balance,
                              @Field("disbursement_date") String disbursementDate);

    @POST("/macros/s/AKfycbzCryw_kR4EB4wrh74a-bFyYHVWmZ7sWEvyTZnClfXYaOK9yWM/exec?action=delete_user_details")
    @FormUrlEncoded
    Call<Customer> deleteUser(@Field("account_number") String accountNumber);
}