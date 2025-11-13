package com.example.carrental.network;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static Retrofit getRetrofit(Context context) {
        if (retrofit == null) {

            // Xác định app đang chạy ở chế độ debug hay không (an toàn cho mọi module)
            boolean isDebuggable = (context.getApplicationInfo().flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0;

            // Logging interceptor (chỉ bật BODY khi debug)
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            if (isDebuggable) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            } else {
                logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            }

            OkHttpClient client = new OkHttpClient.Builder()
                    // Thêm token interceptor trước
                    .addInterceptor(new AuthInterceptor(context))

                    // Tắt Accept-Encoding (gỡ gzip) để tránh các vấn đề encoding không đồng bộ
                    .addNetworkInterceptor(chain -> {
                        Request newRequest = chain.request().newBuilder()
                                .removeHeader("Accept-Encoding")
                                .build();
                        return chain.proceed(newRequest);
                    })

                    // Logging interceptor đặt sau AuthInterceptor
                    .addInterceptor(logging)

                    // Timeout và retry
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)

                    .build();

            // Gson với LocalDateTime adapter nếu cần
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static <T> T createService(Context context, Class<T> serviceClass) {
        return getRetrofit(context).create(serviceClass);
    }
}
