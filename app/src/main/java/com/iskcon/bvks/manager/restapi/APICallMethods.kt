package com.iskcon.bvks.manager.restapi

import com.iskcon.bvks.model.RazorPayOrderResponse
import com.iskcon.bvks.model.RazorPayRequest
import io.reactivex.Single
import retrofit2.http.*


/**
 * @AUTHOR Amandeep Singh
 * @date 05/01/2021
 * */
interface APICallMethods {

    @POST("orders")
    @Headers(
            "Content-Type:application/json",
            "Authorization:Basic cnpwX3Rlc3RfSWpTS2t4b1dXWnlDMWI6dkR5Ukk4NFl4V3ptNk5pOEhYZDJYd3g5"
    )
    fun generateOrders(@Body request: RazorPayRequest): Single<RazorPayOrderResponse>
}
