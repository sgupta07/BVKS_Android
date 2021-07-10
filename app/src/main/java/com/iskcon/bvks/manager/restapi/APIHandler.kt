package com.iskcon.bvks.manager.restapi



import com.iskcon.bvks.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * @AUTHOR Amandeep Singh
 * @date 27/03/2021
 * */
class APIHandler private constructor(baseUrl: String) {

    val handler: APICallMethods

    init {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        //create okhttp client
        val client = OkHttpClient.Builder().addInterceptor(interceptor)
            .readTimeout(100, TimeUnit.SECONDS)
            .connectTimeout(25, TimeUnit.SECONDS)
            .build()
        // create retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        handler = retrofit.create<APICallMethods>(APICallMethods::class.java)
    }

    companion object {
        private var instance: APIHandler? = null

        /**
         * Gets instance.
         * @return the instance of APIHandler
         */
        fun getInstance(): APIHandler {
            if (instance == null) {
                synchronized(APIHandler::class.java) {
                    if (instance == null) {
                        instance =
                            APIHandler(Constants.BASE_URL)
                    }
                }
            }
            return instance!!
        }
    }


}
