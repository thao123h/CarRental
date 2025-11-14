package com.example.carrental.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrental.R;
import com.example.carrental.activities.MainActivity;

import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.JwtResponse;
import com.example.carrental.modals.auth.LoginRequest;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.AuthApiService;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvGoToSignup;

    private AuthApiService apiService;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.login, container, false);

        // Khởi tạo API service và TokenManager
        apiService = RetrofitClient.createService(requireContext(), AuthApiService.class);
        tokenManager = new TokenManager(requireContext());

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvGoToSignup = view.findViewById(R.id.tvSignUpLink);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, password, deviceId);
            }
        });
        tvGoToSignup.setOnClickListener(v -> {
            // Tạo instance SignupFragment
            SignupFragment signupFragment = new SignupFragment();

            // Chuyển sang SignupFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, signupFragment)
                    .addToBackStack(null) // cho phép nhấn back quay lại LoginFragment
                    .commit();
        });


        return view;
    }

    private void performLogin(String email, String password, String deviceId) {
        LoginRequest loginRequest = new LoginRequest(email, password, deviceId);

        apiService.authenticateUser(loginRequest).enqueue(new Callback<BaseResponse<JwtResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<JwtResponse>> call, Response<BaseResponse<JwtResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    JwtResponse jwtResponse = response.body().getData();

                    tokenManager.saveToken(jwtResponse.getToken());
                    tokenManager.saveRefreshToken(jwtResponse.getRefreshToken());
                    tokenManager.saveUsername(jwtResponse.getUsername());
                    tokenManager.saveEmail(jwtResponse.getEmail());
                    tokenManager.saveRoles(jwtResponse.getRoles());

                    Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    String message = "Sai email hoặc mật khẩu";
                    if (response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<JwtResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
