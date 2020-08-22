package com.example.debtorcreditormanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.debtorcreditormanager.MainActivity.moneyDue;
import static com.example.debtorcreditormanager.MainActivity.moneyInBusiness;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {

    private Context mContext;
    private List<CustomerRecord> list = new ArrayList<>();
    private List<CustomerRecord> list2 = new ArrayList<>();
    private String state;


    public void setCustomersList(List<CustomerRecord> customersList, String state) {

        this.state = state;
        if (state.equals("")) {
            moneyInBusiness = 0;
            moneyDue = 0;
        }
        this.list = customersList;
        if (list != null) {
            list2 = new ArrayList<>(list);
        }
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

        long dayDifference;
        if (customer != null) {
            if (state.equals("Disbursement")) {
                holder.balance.setText("DISB: #" + String.valueOf(customer.getDisbursement()));

                holder.dateLastDisbursed.setText(customer.getDisbursementDate());
                holder.customerName.setText(customer.getCustomerName());

                holder.balance.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                holder.dateLastDisbursed.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                holder.customerName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));

            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                DateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");

                try {
                    Date d1 = dateFormat.parse(customer.getDisbursementDate());
                    Date d2 = dateFormat.parse(dateFormat2.format(new Date()));

                    long difference = d2.getTime() - d1.getTime();
                    dayDifference = TimeUnit.MILLISECONDS.toDays(difference);
                    Log.i("TIMEDIFFER", difference + "/" + dayDifference);

                    if (dayDifference >= 31 && Integer.valueOf(customer.getBalance()) > 0){
                        holder.balance.setTextColor(mContext.getResources().getColor(R.color.due));
                        holder.dateLastDisbursed.setTextColor(mContext.getResources().getColor(R.color.due));
                        holder.customerName.setTextColor(mContext.getResources().getColor(R.color.due));

                        moneyDue += Integer.valueOf(customer.getBalance());
                    } else if (dayDifference == 30 && Integer.valueOf(customer.getBalance()) > 0){
                        holder.balance.setTextColor(mContext.getResources().getColor(R.color.about));
                        holder.dateLastDisbursed.setTextColor(mContext.getResources().getColor(R.color.about));
                        holder.customerName.setTextColor(mContext.getResources().getColor(R.color.about));

                        moneyDue += Integer.valueOf(customer.getBalance());
                    } else {
                        holder.balance.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                        holder.dateLastDisbursed.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                        holder.customerName.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
                    }
                } catch (ParseException pe){
                    pe.printStackTrace();
                }


                holder.balance.setText("BAL: #" + String.valueOf(customer.getBalance()));
                holder.dateLastDisbursed.setText(customer.getDisbursementDate());
                holder.customerName.setText(customer.getAccountNumber() + ". " + customer.getCustomerName());

                moneyInBusiness += Integer.valueOf(customer.getBalance());
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

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }

    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<CustomerRecord> filteredList = new ArrayList<>();
            Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
            boolean isNumber = false;
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(list2);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                if (pattern.matcher(filterPattern).matches()) {
                    isNumber = true;
                }

                if (isNumber) {
                    Toast.makeText(mContext, "A", Toast.LENGTH_SHORT).show();
                    for (CustomerRecord customerRecord : list2) {
                        if (customerRecord.getAccountNumber() == Integer.valueOf(filterPattern)) {
                            filteredList.add(customerRecord);
                        }
                    }
                } else {
                    Toast.makeText(mContext, "B", Toast.LENGTH_SHORT).show();
                    for (CustomerRecord customerR : list2) {
                        Log.i("CUSTOMERS_NAMES", customerR.getCustomerName() + " " + filterPattern);
                        if (customerR.getCustomerName().toLowerCase().startsWith(filterPattern)) {
                            filteredList.add(customerR);
                        }
                    }
                }


            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


}

