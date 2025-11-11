//package com.example.carrental.network;
//
//import android.content.Context;
//
//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//
//public class RetrofitClient {
//    private static Retrofit retrofit = null;
//    private static final String BASE_URL = "http://10.0.2.2:8083/";
//
//    private static Retrofit getRetrofit(Context context) {
//        if (retrofit == null) {
//            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//            OkHttpClient client = new OkHttpClient.Builder()
//                    .addInterceptor(new AuthInterceptor(context)) // tự động thêm token
//                    .addInterceptor(logging)
//                    .build();
//
//            retrofit = new Retrofit.Builder()
//                    .baseUrl(BASE_URL)
//                    .client(client)
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//        }
//        return retrofit;
//    }
//    public static <T> T createService(Context context, Class<T> serviceClass) {
//        return getRetrofit(context).create(serviceClass);
//    }
//}


package com.example.carrental.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Không dùng biến static Retrofit nữa để đảm bảo mỗi lần tạo service đều có interceptor mới nhất
    // private static Retrofit retrofit = null;

    private static final String BASE_URL = "http://10.0.2.2:8083/";

    /**
     * Phương thức này sẽ tạo ra một service với một instance Retrofit mới mỗi lần được gọi.
     * Điều này đảm bảo AuthInterceptor luôn được tạo và lấy token mới nhất.
     */
    public static <T> T createService(Context context, Class<T> serviceClass) {
        // 1. Tạo HttpLoggingInterceptor để xem log
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 2. Tạo AuthInterceptor mà bạn đã có
        AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

        // 3. Xây dựng OkHttpClient với cả hai interceptor
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor) // Tự động thêm token
                .addInterceptor(logging)         // Ghi log request/response
                .build();

        // 4. Xây dựng một đối tượng Retrofit MỚI với client đã được cấu hình ở trên
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 5. Tạo và trả về service
        return retrofit.create(serviceClass);
    }
}

