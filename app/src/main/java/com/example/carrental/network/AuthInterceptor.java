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
        String token =
                "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJIb2FuZyBMYW0iLCJqdGkiOiI3N2I3OWIzNS05ZDE5LTQ0YjItYjAwYy1iN2M1MzhhN2ViZTUiLCJpZCI6MTMsImRldmljZUlkIjoidGVzdC1kZXZpY2UtMDAxIiwidG9rZW5WZXJzaW9uIjowLCJyb2xlcyI6WyJSRU5URVIiXSwiaWF0IjoxNzYyODM4ODI1LCJleHAiOjE3NjI4Mzg4ODV9.VIK47E65qsAaf8S-0ONSvxapbVqMb1BO-kXpc3UVCYY";
        Request original = chain.request();
        Request.Builder builder = original.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}