package com.example.carrental.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.MessageResponse;
import com.example.carrental.modals.auth.SignupRequest;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends Fragment {

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private CheckBox cbAgree;
    private Button btnCreateAccount;
    private TextView tvSignIn;
    private AuthApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_signup, container, false);

        // Khởi tạo ApiService
        apiService = RetrofitClient.createService(requireContext(), AuthApiService.class);

        // Ánh xạ View
        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        cbAgree = view.findViewById(R.id.cbAgree);
        btnCreateAccount = view.findViewById(R.id.btnCreateAccount);
        tvSignIn = view.findViewById(R.id.tvSignIn);

        // Xử lý sự kiện click Đăng ký
        btnCreateAccount.setOnClickListener(v -> validateAndSignup());

        // Xử lý sự kiện click "Sign in" -> chuyển sang LoginFragment
        tvSignIn.setOnClickListener(v -> {
            LoginFragment loginFragment = new LoginFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, loginFragment)
                    .addToBackStack(null) // có thể nhấn back quay lại Signup
                    .commit();
        });

        return view;
    }

    private void validateAndSignup() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // 1. Kiểm tra các trường trống
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra mật khẩu có khớp không
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        // 3. Kiểm tra checkbox điều khoản
        if (!cbAgree.isChecked()) {
            Toast.makeText(getContext(), "You must agree to the Terms and Privacy Policy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tất cả hợp lệ -> Gọi API
        performSignup(name, email, password);
    }

    private void performSignup(String name, String email, String password) {
        SignupRequest signupRequest = new SignupRequest(email, password, name);

        apiService.registerUser(signupRequest).enqueue(new Callback<BaseResponse<MessageResponse>>() {
            @Override
            public void onResponse(Call<BaseResponse<MessageResponse>> call, Response<BaseResponse<MessageResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Account created! Please sign in.", Toast.LENGTH_LONG).show();

                    // Chuyển sang LoginFragment sau khi signup thành công
                    LoginFragment loginFragment = new LoginFragment();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, loginFragment)
                            .commit();

                } else {
                    String message = "Sign up failed";
                    if(response.body() != null && response.body().getMessage() != null) {
                        message = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        message = "Error: " + response.code();
                    }
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<MessageResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
