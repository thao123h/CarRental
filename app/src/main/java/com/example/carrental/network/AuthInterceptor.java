package com.example.carrental.network;


import android.content.Context;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
//        TokenManager tokenManager = new TokenManager(context);
//        String token = tokenManager.getToken(); // luôn lấy token mới nhất
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGFvb29vIiwiaWQiOjE3LCJ0b2tlblZlcnNpb24iOjAsInJvbGVzIjpbIlJFTlRFUiJdLCJpYXQiOjE3NjI4NDY5MjksImV4cCI6MTc2Mjg1NDEyOX0.2vmFpAJZBqJCfP1S2FW373ccM8iNA-RinsHOAuPHx4k";
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
