package com.example.debtorcreditormanager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Info {
    @SerializedName("items")
    @Expose
    private List<Customer> items = null;

    public List<Customer> getItems() {
        return items;
    }

    public void setItems(List<Customer> items) {
        this.items = items;
    }
}