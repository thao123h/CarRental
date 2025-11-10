package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrental.fragments.AccountFragment;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.JwtResponse;
import com.example.carrental.modals.auth.LoginRequest;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etEmailOrPhone;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToSignup;

    private AuthApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        // Khởi tạo
        apiService = RetrofitClient.createService(this, AuthApiService.class);
        tokenManager = new TokenManager(this);

        // Mặc dù ID là etPhoneNumber, chúng ta coi nó là nơi nhập Email cho API
        etEmail = findViewById(R.id.etEmail);

        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);

        // Link "đăng ký ngay" của bạn có ID là tvSignUpLink
        tvGoToSignup = findViewById(R.id.tvSignUpLink);

        // Xử lý sự kiện click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString();
            String password = etPassword.getText().toString();

            // Lấy deviceId
            String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, password, deviceId);
            }
        });

        tvGoToSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });

    }

    private void performLogin(String email, String password, String deviceId) {
        LoginRequest loginRequest = new LoginRequest(email, password, deviceId);

        apiService.authenticateUser(loginRequest).enqueue(new Callback<BaseResponse<JwtResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<JwtResponse>> call, Response<BaseResponse<JwtResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Đăng nhập thành công
                    JwtResponse jwtResponse = response.body().getData();

                    // LƯU TOKEN LẠI
                    tokenManager.saveToken(jwtResponse.getToken());

                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển sang màn hình Profile
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    // Xử lý lỗi (sai mật khẩu,...)
                    String message = "Sai email hoặc mật khẩu";
                    if (response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<JwtResponse>> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}