package com.iskcon.bvks.model


import com.google.gson.annotations.SerializedName

data class RazorPayOrderResponse(
    @SerializedName("amount")
    val amount: Int,
    @SerializedName("amount_due")
    val amountDue: Int,
    @SerializedName("amount_paid")
    val amountPaid: Int,
    @SerializedName("attempts")
    val attempts: Int,
    @SerializedName("created_at")
    val createdAt: Int,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("entity")
    val entity: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("notes")
    val notes: List<Any>,
    @SerializedName("receipt")
    val receipt: String,
    @SerializedName("status")
    val status: String
)