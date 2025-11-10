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
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken(); // luôn lấy token mới nhất
//        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0aGFvIiwianRpIjoiMGJiYzRlMGYtNmZhMC00YWFjLWFiZTYtOGY1OGEyOWM2NzBmIiwiaWQiOjE1LCJ0b2tlblZlcnNpb24iOjAsInJvbGVzIjpbIlJFTlRFUiJdLCJpYXQiOjE3NjI1ODU5NTYsImV4cCI6MTc2MjU4NjAxNn0.PkXErplOs_e5qQjVPJ4RPYMv3Ek9iPnTFVtjXOKOCCw";
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
