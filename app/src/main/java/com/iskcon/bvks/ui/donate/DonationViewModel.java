package com.iskcon.bvks.ui.donate;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.iskcon.bvks.base.BvksApplication;
import com.iskcon.bvks.model.RazorPayOrderResponse;
import com.iskcon.bvks.model.RazorPayRequest;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DonationViewModel extends AndroidViewModel {

    private MutableLiveData<RazorPayOrderResponse> responseMutableLiveData;

    public DonationViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<RazorPayOrderResponse> getResponseMutableLiveData() {
        if (responseMutableLiveData == null) {
            responseMutableLiveData = new MutableLiveData<>();
        }
        return responseMutableLiveData;
    }

    public void generateOrderId(int amount, String currency, String receipt) {
        RazorPayRequest razorPayRequest=new RazorPayRequest(amount, currency, receipt);
        ((BvksApplication) getApplication()).getHandler().generateOrders(razorPayRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<RazorPayOrderResponse>() {

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull RazorPayOrderResponse razorPayOrderResponse) {

                        if (razorPayOrderResponse != null) {
                            getResponseMutableLiveData().postValue(razorPayOrderResponse);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                    }
                });

    }
}
