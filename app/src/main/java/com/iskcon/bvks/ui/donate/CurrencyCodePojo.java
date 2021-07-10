package com.iskcon.bvks.ui.donate;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @AUTHOR Amandeep Singh
 * @date 27/01/2021
 */


public class CurrencyCodePojo {

    @SerializedName("data")
    @Expose
    private List<Data> data = null;

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public class Data {

        @SerializedName("currency_code")
        @Expose
        private String currencyCode;
        @SerializedName("currency_symbol")
        @Expose
        private String currencySymbol;
        @SerializedName("currency_name")
        @Expose
        private String currencyName;
        @SerializedName("country_code")
        @Expose
        private String countryCode;

        public String getCurrencyCode() {
            return currencyCode;
        }

        public void setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
        }

        public String getCurrencySymbol() {
            return currencySymbol;
        }

        public void setCurrencySymbol(String currencySymbol) {
            this.currencySymbol = currencySymbol;
        }

        public String getCurrencyName() {
            return currencyName;
        }

        public void setCurrencyName(String currencyName) {
            this.currencyName = currencyName;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

    }
}
