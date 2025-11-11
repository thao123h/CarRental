package com.example.carrental.activities; // Thay bằng package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Import các class của bạn
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.MessageResponse;
import com.example.carrental.modals.auth.SignupRequest;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbAgree;
    private Button btnCreateAccount;
    private TextView tvSignIn;
    private AuthApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đặt layout mới
        setContentView(R.layout.activity_signup);

        // Khởi tạo ApiService
        apiService = RetrofitClient.createService(this, AuthApiService.class);

        // Ánh xạ View từ layout mới
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbAgree = findViewById(R.id.cbAgree);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvSignIn = findViewById(R.id.tvSignIn);

        // Xử lý sự kiện click Đăng ký
        btnCreateAccount.setOnClickListener(v -> validateAndSignup());

        // Xử lý sự kiện click "Sign in"
        tvSignIn.setOnClickListener(v -> {
            // Quay lại màn hình Login
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Đóng màn hình này
        });
    }

    private void validateAndSignup() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra các trường trống
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // 3. Kiểm tra checkbox điều khoản
        if (!cbAgree.isChecked()) {
            Toast.makeText(this, "You must agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tất cả đều hợp lệ -> Gọi API
        performSignup(name, email, password);
    }

    private void performSignup(String name, String email, String password) {
        // Dùng đúng model SignupRequest của bạn
        SignupRequest signupRequest = new SignupRequest(email, password, name);

        // Giả sử API của bạn chạy ở /auth/signup (do có Gateway)
        // Nếu không, hãy sửa lại trong RetrofitClient hoặc AuthApiService
        apiService.registerUser(signupRequest).enqueue(new Callback<BaseResponse<MessageResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MessageResponse>> call, Response<BaseResponse<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Đăng ký thành công
                    Toast.makeText(SignupActivity.this, "Account created! Please sign in.", Toast.LENGTH_LONG).show();

                    // Chuyển sang màn hình Login
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finishAffinity(); // Đóng màn hình này và các màn hình trước đó (nếu có)
                } else {
                    // Xử lý lỗi (ví dụ: email đã tồn tại)
                    String message = "Sign up failed";
                    if(response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        message = "Error: " + response.code(); // Lỗi 4xx, 5xx
                    }
                    Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MessageResponse>> call, Throwable t) {
                // Lỗi mạng
                Toast.makeText(SignupActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}