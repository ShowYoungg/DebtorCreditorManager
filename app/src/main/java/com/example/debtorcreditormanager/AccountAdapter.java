package com.example.debtorcreditormanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ProductViewHolder> {

    private Context mContext;
    private List<Customer> list = new ArrayList<>();

    public void setAccountList(List<Customer> customersList) {
        this.list = customersList;
        notifyDataSetChanged();
    }


    public AccountAdapter(Context context) {
        this.mContext = context;
        notifyDataSetChanged();
    }


    @Override
    public AccountAdapter.ProductViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.account_list_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        AccountAdapter.ProductViewHolder viewHolder = new AccountAdapter.ProductViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final AccountAdapter.ProductViewHolder holder, final int position) {
        //Always declare your object inside here; never set it as a global object
        final Customer customer = list.get(position);

        if (customer != null) {
            if (customer.getPayback().equals("0")) {
                holder.description.setText("Disbursement " + customer.getDate());
                holder.amount.setText(customer.getDisbursement());

                holder.description.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                holder.amount.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            } else {
                holder.description.setText("Repayment on " + customer.getDate());
                holder.amount.setText(customer.getPayback());

                holder.description.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                holder.amount.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private TextView description;
        private TextView amount;

        public ProductViewHolder(View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.description);
            amount = itemView.findViewById(R.id.amount);
        }
    }
}
