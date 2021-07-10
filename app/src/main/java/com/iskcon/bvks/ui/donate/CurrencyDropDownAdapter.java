package com.iskcon.bvks.ui.donate;

import android.annotation.SuppressLint;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.RowListDropDownBinding;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Amandeep Singh
 * @date 26/01/2021
 */
public class CurrencyDropDownAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<CurrencyCodePojo.Data> arrDropDown;
    private List<CurrencyCodePojo.Data> arrDropDownFiltered;
    private DropDownClickListener mListener;

    public CurrencyDropDownAdapter(List<CurrencyCodePojo.Data> arrDropDown, DropDownClickListener listener) {
        this.arrDropDown = arrDropDown;
        this.arrDropDownFiltered=arrDropDown;
        mListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowListDropDownBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.row_list_drop_down, parent, false);
        return new MyViewHolder(binding);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyViewHolder) {

            ((MyViewHolder) holder).binding.tvName.setText(arrDropDownFiltered.get(position).getCurrencyCode()+" ("+arrDropDownFiltered.get(position).getCurrencyName()+")");
            ((MyViewHolder) holder).binding.tvSrn.setText(String.format("%s.", String.valueOf(position + 1)));
            ((MyViewHolder) holder).binding.main.setOnClickListener(v ->
                    mListener.onClick(position, arrDropDownFiltered.get(position)));

        }
    }

    @Override
    public int getItemCount() {
        return (arrDropDownFiltered == null) ? 0 : arrDropDownFiltered.size();


    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    arrDropDownFiltered = arrDropDown;
                } else {
                    List<CurrencyCodePojo.Data> filteredList = new ArrayList<>();
                    for (CurrencyCodePojo.Data row : arrDropDown) {

                        if (row.getCurrencyName().toLowerCase().contains(charString.toLowerCase()) || row.getCurrencyCode().toLowerCase().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    arrDropDownFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = arrDropDownFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                arrDropDownFiltered = (ArrayList<CurrencyCodePojo.Data>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface DropDownClickListener {
        void onClick(int position, CurrencyCodePojo.Data item);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        RowListDropDownBinding binding;

        MyViewHolder(RowListDropDownBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

    }
}


