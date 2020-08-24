package com.example.debtorcreditormanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class SummaryAdapter extends RecyclerView.Adapter<SummaryAdapter.ProductViewHolder> {

    private Context mContext;
    private List<String[]> list;

    public void setTodaySummary(List<String[]> list) {
        this.list = list;
        notifyDataSetChanged();
    }


    public SummaryAdapter(Context context) {
        this.mContext = context;
        notifyDataSetChanged();
    }


    @Override
    public SummaryAdapter.ProductViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.summary_list;

        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        SummaryAdapter.ProductViewHolder viewHolder = new SummaryAdapter.ProductViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final SummaryAdapter.ProductViewHolder holder, final int position) {
        //Always declare your object inside here; never set it as a global object
        final String[] summary = list.get(position);
        Log.i("ADAPTERLIST","" + summary[0] + "/" + summary[1]);

        if (summary != null) {
            holder.todayDate.setText(summary[0]);
            holder.todayDisbursement.setText("Disbursement: " + summary[1]);
            holder.todayRepayment.setText("Repayment: " + summary[2]);
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

        private TextView todayDate;
        private TextView todayDisbursement;
        private TextView todayRepayment;
        private LinearLayout layout;

        public ProductViewHolder(View itemView) {
            super(itemView);

            todayDate = itemView.findViewById(R.id.date_title);
            todayDisbursement = itemView.findViewById(R.id.today_disbursement);
            todayRepayment = itemView.findViewById(R.id.today_repayment);
            layout = itemView.findViewById(R.id.summary_layout);
        }
    }
}
