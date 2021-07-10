package com.iskcon.bvks.ui.donate;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.iskcon.bvks.R;
import com.iskcon.bvks.databinding.FragmentDonateBinding;
import com.iskcon.bvks.model.RazorPayOrderResponse;
import com.iskcon.bvks.util.Constants;
import com.iskcon.bvks.util.Utils;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class DonateFragment extends Fragment {
    private FragmentDonateBinding mBinding;
    private CurrencyCodePojo.Data mCurrency = null;
    private DonationViewModel mDonationViewModel;
    Checkout checkout;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkout=new Checkout();
        mDonationViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication())).get(DonationViewModel.class);
        Checkout.preload(getActivity().getApplicationContext());
        checkout.setKeyID(Constants.RAZOR_PAY_TEST_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_donate, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding.etCurrency.setOnClickListener(v ->
                DonateUtils.getInstance().showCurrencyCodeDropDown(getActivity(), (position, item) -> {
                            mCurrency = item;
                            mBinding.etCurrency.setText(item.getCurrencyCode() + " (" + item.getCurrencyName() + ")");
                        }
                ));

        mBinding.etDonateFor.setOnClickListener(v -> DonateUtils.getInstance().showDropDown(getActivity(), "Donate For", Constants.ARR_DONATION_FOR, new DonateUtils.DropDownClickListener() {
            @Override
            public void onSelect(int position, String option) {
                mBinding.etDonateFor.setText(option);
                if (option.equals("Other(s) (Please specify)")) {
                    mBinding.etOther.setVisibility(View.VISIBLE);
                } else {
                    mBinding.etOther.setVisibility(View.GONE);
                }
            }
        }));

        mBinding.btnContinue.setOnClickListener(v -> {
            if (isDataValidate()) {
                Utils.getInstance().hideKeyboard(getContext(), mBinding.getRoot());
                int amount = Integer.parseInt(mBinding.etAmount.getText().toString())*100;
                String donationFor = mBinding.etDonateFor.getText().toString();
                String receipt = Utils.getInstance().getAlphaNumericString(10)+"_#"+donationFor;
                mDonationViewModel.generateOrderId(amount,mCurrency.getCurrencyCode(),receipt);

            }
        });

        mDonationViewModel.getResponseMutableLiveData().observe(this, new Observer<RazorPayOrderResponse>() {
            @Override
            public void onChanged(RazorPayOrderResponse razorPayOrderResponse) {
                if (razorPayOrderResponse!=null){
                   startPayment(razorPayOrderResponse);

                }
            }
        });
    }
    public void startPayment(RazorPayOrderResponse razorPayOrderResponse) {
        checkout.setImage(R.drawable.logo);
        try {
            JSONObject options = new JSONObject();
            options.put("name", "Bhakti Vikasa Trust");
            options.put("description", "Donation");
            options.put("image", "https://bvks-d1ac.kxcdn.com/wp-content/uploads/2018/05/20151354/4-3.jpg");
            options.put("order_id", razorPayOrderResponse.getId());//from response of step 3.
            options.put("theme.color", "#Fd7e14");
            options.put("currency", razorPayOrderResponse.getCurrency());
            options.put("amount", razorPayOrderResponse.getAmountPaid());//pass amount in currency subunits
            options.put("prefill.email", mBinding.etEmail.getText().toString());
            options.put("prefill.contact",mBinding.etPhone.getText().toString());
            checkout.open(getActivity(), options);

        } catch(Exception e) {
            Log.e("DONATION_FRAGMENT", "Error in starting Razorpay Checkout", e);
        }
    }
    private boolean isDataValidate() {
        boolean isValid = false;
        String amount = mBinding.etAmount.getText().toString();
        String donationFor = mBinding.etDonateFor.getText().toString();
        if (!TextUtils.isEmpty(amount) && mCurrency != null&&!TextUtils.isEmpty(donationFor) ) {
            isValid = true;
        } else {
            if (TextUtils.isEmpty(amount)) {
                Utils.getInstance().showToast(getContext(), "Please provide valid amount");
                isValid = false;
            } else if (mCurrency == null) {
                Utils.getInstance().showToast(getContext(), "Please provide a valid currency");
                isValid = false;
            }else if (TextUtils.isEmpty(donationFor)) {
                Utils.getInstance().showToast(getContext(), "Please select donation option");
                isValid = false;
            }
        }
        return isValid;
    }


}