package com.iskcon.bvks.util;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.RowListDropDownSortBinding;


/**
 * @AUTHOR Amandeep Singh
 * @date 16/02/2021
 */
public class DropDownSortAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] arrDropDown;
    private DropDownClickListener mListener;
    private int mSelectedPosition;

    public DropDownSortAdapter(String[] arrDropDown,int selectedPosition, DropDownClickListener listener) {
        this.arrDropDown = arrDropDown;
        mListener = listener;
        mSelectedPosition = selectedPosition;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowListDropDownSortBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.row_list_drop_down_sort, parent, false);
        return new DropDownSortAdapter.MyViewHolder(binding);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DropDownSortAdapter.MyViewHolder) {
            ((DropDownSortAdapter.MyViewHolder) holder).binding.setSelected(mSelectedPosition == position);
            ((DropDownSortAdapter.MyViewHolder) holder).binding.tvName.setText(arrDropDown[position]);
            ((DropDownSortAdapter.MyViewHolder) holder).binding.main.setOnClickListener(v ->
                    mListener.onClick(position, arrDropDown[position]));

        }
    }

    @Override
    public int getItemCount() {
        return (arrDropDown == null) ? 0 : arrDropDown.length;


    }

    public interface DropDownClickListener {
        void onClick(int position, String option);
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        RowListDropDownSortBinding binding;

        MyViewHolder(RowListDropDownSortBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

    }
}



