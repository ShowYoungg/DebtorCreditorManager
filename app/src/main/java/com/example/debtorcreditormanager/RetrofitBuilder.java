
package com.example.debtorcreditormanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public final class RetrofitBuilder {
    static GetDataService recipeInterface;

    public static GetDataService Retrieve() {

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        recipeInterface = new Retrofit.Builder()
                .baseUrl("https://script.google.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build())
                .build().create(GetDataService.class);

        return recipeInterface;
    }
}

