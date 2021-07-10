package com.iskcon.bvks.ui.donate;

import android.annotation.SuppressLint;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.RowListDropDownBinding;


/**
 * @author Amandeep Singh
 * @date 27/01/2021
 */
public class DropDownAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String[] arrDropDown;
    private DropDownClickListener mListener;

    public DropDownAdapter(String[] arrDropDown, DropDownClickListener listener) {
        this.arrDropDown = arrDropDown;
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
            ((MyViewHolder) holder).binding.tvName.setText(arrDropDown[position]);
            ((MyViewHolder) holder).binding.tvSrn.setText(String.format("%s.", String.valueOf(position + 1)));
            ((MyViewHolder) holder).binding.main.setOnClickListener(v ->
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
        RowListDropDownBinding binding;

        MyViewHolder(RowListDropDownBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

    }
}


