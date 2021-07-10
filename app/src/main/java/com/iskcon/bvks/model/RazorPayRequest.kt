package com.iskcon.bvks.model


import com.google.gson.annotations.SerializedName

data class RazorPayRequest(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("receipt")
    val receipt: String
)