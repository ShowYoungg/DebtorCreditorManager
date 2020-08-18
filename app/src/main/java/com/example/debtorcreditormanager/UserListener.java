package com.example.debtorcreditormanager;

import java.util.List;

public interface UserListener {
    void onStarted();
    void onSuccess(List<Customer> customers);
    void onFailed(String message);

}