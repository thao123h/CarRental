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
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGFvb29vIiwianRpIjoiZTcxOTdlNDMtZjc5Ni00ZDNhLWI0ODMtZWZmNzIwZjA0YzVkIiwiaWQiOjE3LCJ0b2tlblZlcnNpb24iOjAsInJvbGVzIjpbIlJFTlRFUiJdLCJpYXQiOjE3NjI3ODcyNDAsImV4cCI6MTc2Mjc4NzMwMH0.t3dnOr5p6y1ZwZuZDRoc-HMSO3NkYmNz1YBl2EcuOmE";
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
