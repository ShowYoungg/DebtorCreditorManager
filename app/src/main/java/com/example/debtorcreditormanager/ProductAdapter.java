package com.example.debtorcreditormanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context mContext;
    private List<CustomerRecord> list = new ArrayList<>();
    private String state;

    public void setCustomersList(List<CustomerRecord> customersList, String state) {
        this.state = state;
        this.list = customersList;
        notifyDataSetChanged();
    }


    public ProductAdapter(Context context) {
        this.mContext = context;
        notifyDataSetChanged();
    }


    @Override
    public ProductViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.customer_display;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        ProductAdapter.ProductViewHolder viewHolder = new ProductAdapter.ProductViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        //Always declare your object inside here; never set it as a global object
        final CustomerRecord customer = list.get(position);

        if (customer != null) {
            if (state.equals("Disbursement")) {
                holder.balance.setText("DISB: #" + String.valueOf(customer.getDisbursement()));

                holder.dateLastDisbursed.setText(customer.getDisbursementDate());
                holder.customerName.setText(customer.getCustomerName());
            } else if (state.equals("Recent")){

                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                String recentMonth = String.valueOf(Integer.valueOf(cal.get(Calendar.MONTH)) + 1);
                String year = String.valueOf(cal.get(Calendar.YEAR));

                String m = customer.getDisbursementDate();
                String[] mm = m.split("/");

                String disbursementMonth = String.valueOf(Integer.valueOf(mm[1]));

                Log.i("DATE_DIFFERENCE", disbursementMonth + "/" + recentMonth);

                if (disbursementMonth.equals(recentMonth)){
                    holder.dateLastDisbursed.setText(customer.getDisbursementDate());
                    holder.customerName.setText(customer.getCustomerName());

                    holder.balance.setText("DISB: #" + String.valueOf(customer.getBalance()));
                }
            } else {
                holder.balance.setText("BAL: #" + String.valueOf(customer.getBalance()));
                holder.dateLastDisbursed.setText(customer.getDisbursementDate());
                holder.customerName.setText(customer.getCustomerName());
            }
        }

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (customer != null && Integer.valueOf(customer.getBalance()) == 0) {
                    updateDisburseDialog(customer.getAccountNumber(), customer.getCustomerName());
                } else {
                    mContext.startActivity(new Intent(mContext, AccountActivity.class).putExtra("CustomerRecord", customer));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private TextView customerName;
        private TextView dateLastDisbursed;
        private TextView balance;
        private RelativeLayout layout;

        public ProductViewHolder(View itemView) {
            super(itemView);

            customerName = itemView.findViewById(R.id.name);
            dateLastDisbursed = itemView.findViewById(R.id.date);
            balance = itemView.findViewById(R.id.balance);
            layout = itemView.findViewById(R.id.layout);
        }
    }

    public void updateDisburseDialog(int account, String name) {
        new AlertDialog.Builder(mContext)
                .setTitle("Disburse First")
                .setMessage("You can't perform a transaction on this customer until you disburse. Are you ready to disburse?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mContext.startActivity(new Intent(mContext, AddEntryActivity.class)
                                .putExtra("updatePosition", account).putExtra("customerName", name));
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

}

