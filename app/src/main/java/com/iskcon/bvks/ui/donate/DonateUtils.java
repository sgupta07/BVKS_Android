package com.iskcon.bvks.ui.donate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.LayoutCurrencyDropDownBinding;
import com.iskcon.bvks.databinding.LayoutDropDownBinding;
import com.iskcon.bvks.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @AUTHOR Amandeep Singh
 * @date 26/01/2021
 */
public class DonateUtils {
    private static final DonateUtils ourInstance = new DonateUtils();
    private BottomSheetDialog mBsCurrencyDropDown, mBsDropDown;

    private DonateUtils() {
    }

    public static DonateUtils getInstance() {
        return ourInstance;
    }

    public void     showCurrencyCodeDropDown(Activity activity, CurrencyCodeDropDownInterface dropDownSelectInterface) {
        List<CurrencyCodePojo.Data> dataList = new ArrayList<>();
        Gson gson = new Gson();
        CurrencyCodePojo currencyCodePojo = gson.fromJson(getStringFromLocalJson("currencyCodeRazorPay.json", activity), CurrencyCodePojo.class);
        dataList.addAll(currencyCodePojo.getData());
        mBsCurrencyDropDown = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams")
        LayoutCurrencyDropDownBinding currencyDropDownBinding = DataBindingUtil.inflate(activity.getLayoutInflater(),
                R.layout.layout_currency_drop_down, null, false);
        mBsCurrencyDropDown.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = mBsCurrencyDropDown.getWindow().getAttributes();
        layoutParams.x = 100; // right margin
        layoutParams.y = 170; // top margin
        mBsCurrencyDropDown.getWindow().setAttributes(layoutParams);
        mBsCurrencyDropDown.setContentView(currencyDropDownBinding.getRoot());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);

        currencyDropDownBinding.tvHeading.setText("Select Currency");
        currencyDropDownBinding.rvCurrencyDropDown.setLayoutManager(linearLayoutManager);
        CurrencyDropDownAdapter adapter = new CurrencyDropDownAdapter(dataList, (position, item) -> {
            dropDownSelectInterface.onSelect(position, item);
            Utils.getInstance().hideKeyboard(activity.getApplicationContext(),currencyDropDownBinding.getRoot());
            if (mBsCurrencyDropDown.isShowing()) {
                mBsCurrencyDropDown.dismiss();
            }
        });
        currencyDropDownBinding.rvCurrencyDropDown.setAdapter(adapter);
        currencyDropDownBinding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBsCurrencyDropDown.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                setupFullHeight(bottomSheetDialog, activity);
            }
        });
        mBsCurrencyDropDown.show();
    }

    private void setupFullHeight(BottomSheetDialog bottomSheetDialog, Activity activity) {
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();

        int windowHeight = getWindowHeight(activity);
        if (layoutParams != null) {
            layoutParams.height = windowHeight;
        }
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private int getWindowHeight(Activity activity) {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        (activity).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public void showDropDown(Activity activity, String tittle, String[] arrDropDown, DropDownClickListener dropDownSelectInterface) {
        mBsDropDown = new BottomSheetDialog(activity);
        @SuppressLint("InflateParams")
        LayoutDropDownBinding layoutSavedStoreBinding = DataBindingUtil.inflate(activity.getLayoutInflater(),
                R.layout.layout_drop_down, null, false);
        mBsDropDown.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams layoutParams = mBsDropDown.getWindow().getAttributes();
        layoutParams.x = 100; // right margin
        layoutParams.y = 170; // top margin
        mBsDropDown.getWindow().setAttributes(layoutParams);
        mBsDropDown.setContentView(layoutSavedStoreBinding.getRoot());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false);
        layoutSavedStoreBinding.tvHeading.setText(tittle);
        layoutSavedStoreBinding.rvDropDown.setLayoutManager(linearLayoutManager);
        layoutSavedStoreBinding.rvDropDown.setAdapter(new DropDownAdapter(arrDropDown, new DropDownAdapter.DropDownClickListener() {
            @Override
            public void onClick(int position, String option) {
                dropDownSelectInterface.onSelect(position, option);
                if (mBsDropDown.isShowing()) {
                    mBsDropDown.dismiss();
                }
            }
        }));
        mBsDropDown.show();
    }

    public String getStringFromLocalJson(String assetsFileName, Activity activity) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(activity.getAssets().open(assetsFileName)));
            String temp;
            while ((temp = br.readLine()) != null)
                sb.append(temp);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close(); // stop reading
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }


    public interface CurrencyCodeDropDownInterface {
        void onSelect(int position, CurrencyCodePojo.Data item);
    }

    public interface DropDownClickListener {
        void onSelect(int position, String option);
    }


}
