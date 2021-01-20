package ua.com.expert.validator.sync

import android.content.Context
import com.google.gson.*
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ua.com.expert.validator.BuildConfig
import ua.com.expert.validator.common.Consts.APP_SETTINGS_PREFS
import ua.com.expert.validator.common.Consts.CONNECT_SERVER_URL
import ua.com.expert.validator.common.Consts.CONNECT_TIMEOUT_SECONDS_RETROFIT
import ua.com.expert.validator.common.Consts.SERVER
import ua.com.expert.validator.common.Consts.TYPE_CONNECTION
import ua.com.expert.validator.utils.SharedStorage
import java.lang.reflect.Type
import java.util.*
import java.util.concurrent.TimeUnit


object SyncServiceFactory {
    var httpClient: OkHttpClient.Builder  = OkHttpClient.Builder()
    .readTimeout(CONNECT_TIMEOUT_SECONDS_RETROFIT, TimeUnit.SECONDS)
    .connectTimeout(CONNECT_TIMEOUT_SECONDS_RETROFIT, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)

    private var logging: HttpLoggingInterceptor = HttpLoggingInterceptor()

    private var dateJsonSerializer : JsonSerializer<Date> = JsonSerializer<Date>(
            fun (src : Date, typeOfSrc : Type, context : JsonSerializationContext) : JsonElement? {
                return if (src == null) null else JsonPrimitive(src.getTime())
            })

    private var dateJsonDeserializer : JsonDeserializer<Date> = JsonDeserializer<Date>(
            fun (json : JsonElement, typeOfT : Type, context : JsonDeserializationContext) : Date? {
                return if (json == null) null else Date(json.getAsLong())
            })

    private fun getBuilder(url: String): Retrofit.Builder{
        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(NullOnEmptyConverterFactory())
                .addCallAdapterFactory(
                        RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(
                        GsonBuilder()
                                .registerTypeAdapter(Date::class.java, dateJsonSerializer)
                                .registerTypeAdapter(Date::class.java, dateJsonDeserializer)
                                .create()))
    }

    fun <S> createService(serviceClass : Class<S>, context: Context) : S {
        return buildService(serviceClass, context, 0)
    }

    fun <S> createService(serviceClass : Class<S>, context : Context, timeOut : Long) : S {
        return buildService(serviceClass, context, timeOut)
    }


    private fun <S> buildService(serviceClass : Class<S>, context : Context, timeOut : Long) : S {

      //  var token : String? = SharedStorage.getString(context, APP_CASH_TOKEN_PREFS, TOKEN, "")

        if(BuildConfig.DEBUG){
            logging.level = HttpLoggingInterceptor.Level.BODY
        }

        if(timeOut>0){
            httpClient.readTimeout(timeOut, TimeUnit.SECONDS)
                    .connectTimeout(timeOut, TimeUnit.SECONDS)
        }

        httpClient.addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                            .addHeader("Accept", "application/json")
                          //  .addHeader(TOKEN_HEADER, token)
                            .method(chain.request().method(), chain.request().body())
                            .build()
                    chain.proceed(newRequest)
                }

        httpClient.addInterceptor(logging)

        var client : OkHttpClient = httpClient.build()

        var retrofit : Retrofit

        try {
            retrofit = getBuilder(TYPE_CONNECTION + SharedStorage.getString(context,
                    APP_SETTINGS_PREFS, SERVER, CONNECT_SERVER_URL) + "/")
                    .client(client)
                    .build();
        }catch (e: Exception) {
            retrofit = getBuilder(TYPE_CONNECTION + "localhost/")
                    .client(client)
                    .build();
        }

        return retrofit.create(serviceClass);
    }

    private class NullOnEmptyConverterFactory : Converter.Factory() {

        override fun responseBodyConverter(type: Type?, annotations: Array<Annotation>?, retrofit: Retrofit?): Converter<ResponseBody, *>? {
            val delegate = retrofit!!.nextResponseBodyConverter<Any>(this, type!!, annotations!!)
            return Converter<ResponseBody, Any> { body ->
                if (body.contentLength() == 0L) null else delegate.convert(
                        body
                )
            }
        }
    }
}
