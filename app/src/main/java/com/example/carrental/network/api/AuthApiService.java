package com.example.carrental.network.api;

import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.JwtResponse;
import com.example.carrental.modals.auth.LoginRequest;
import com.example.carrental.modals.auth.MessageResponse;
import com.example.carrental.modals.auth.RefreshTokenRequest;
import com.example.carrental.modals.auth.RoleUpdateRequest;
import com.example.carrental.modals.auth.SignupRequest;
import com.example.carrental.modals.auth.UserDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthApiService {
    // Lấy thông tin người dùng hiện tại (đã đăng nhập)
    @GET("auth/users/me")
    Call<BaseResponse<UserDTO>> getMe();

    // Lấy thông tin người dùng theo ID
    @GET("auth/users/{id}")
    Call<BaseResponse<UserDTO>> getUserById(@Path("id") Long id);

    // Cập nhật thông tin người dùng hiện tại
    @PATCH("auth/users/me")
    Call<BaseResponse<UserDTO>> updateMe(@Body UserDTO userDTO);

    // Đăng nhập (xác thực user, trả JWT token)
    @POST("auth/signin")
    Call<BaseResponse<JwtResponse>> authenticateUser(@Body LoginRequest loginRequest);

    // Đăng ký tài khoản mới
    @POST("auth/signup")
    Call<BaseResponse<MessageResponse>> registerUser(@Body SignupRequest signUpRequest);

    // Refresh Token sau khi thêm Role
    @POST("auth/refresh-role")
    Call<BaseResponse<JwtResponse>> refreshRole(@Body RoleUpdateRequest roleUpdateRequest);

    // Đăng xuất (xóa refresh token và blacklist access token)
    @POST("auth/logoutone")
    Call<BaseResponse<Object>> logout(@Body RefreshTokenRequest refreshTokenRequest);

    // Làm mới Access Token bằng Refresh Token
    @POST("auth/refresh")
    Call<BaseResponse<JwtResponse>> refresh(@Body RefreshTokenRequest refreshTokenRequest);

    // Kiểm tra Email tồn tại
    @FormUrlEncoded
    @POST("auth/check-exists")
    Call<BaseResponse<Boolean>> checkEmailExists(@Field("email") String email);
}