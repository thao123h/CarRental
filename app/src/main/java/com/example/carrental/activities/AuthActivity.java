package com.example.carrental.activities;

import android.app.AppComponentFactory;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.carrental.R;
import com.example.carrental.adapters.ItemAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.AuthApiService;
import com.example.carrental.network.api.ItemService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {
    private AuthApiService api;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        api = RetrofitClient.createService(this, AuthApiService.class);
        api.getMe().enqueue(new Callback<BaseResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserDTO>> call, Response<BaseResponse<UserDTO>> response) {
                if(response.isSuccessful() && response.body()!=null){
                    UserDTO userDTO = response.body().getData();
                    Toast.makeText(AuthActivity.this, "Load error: " + response.code(), Toast.LENGTH_SHORT).show();
                };
            }

            @Override
            public void onFailure(Call<BaseResponse<UserDTO>> call, Throwable t) {
                Toast.makeText(AuthActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

    }

}