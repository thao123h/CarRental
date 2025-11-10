package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

// Import các class của bạn
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private AuthApiService apiService;
    private TokenManager tokenManager;

    // Các View trong Profile Header
    private ImageView ivBack, ivAvatar, ivEdit, ivShare;
    private TextView tvName, tvJoinDate , tvJoinDateValue;


    // Các View cho từng mục
    private View itemLicense, itemPhone, itemEmail, itemFacebook, itemGoogle;
    private Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo
        apiService = RetrofitClient.createService(this, AuthApiService.class);
        tokenManager = new TokenManager(this);

        // Ánh xạ Header
        ivBack = findViewById(R.id.ivBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivEdit = findViewById(R.id.ivEdit);
        ivShare = findViewById(R.id.ivShare);
        tvName = findViewById(R.id.tvName);
        tvJoinDateValue = findViewById(R.id.tvJoinDateValue);
        tvJoinDate = findViewById(R.id.tvJoinDate);

        // Ánh xạ từng mục (lấy view cha)
        itemLicense = findViewById(R.id.itemLicense);
        itemPhone = findViewById(R.id.itemPhone);
        itemEmail = findViewById(R.id.itemEmail);
        itemFacebook = findViewById(R.id.itemFacebook);
        itemGoogle = findViewById(R.id.itemGoogle);
        btnLogout = findViewById(R.id.btnLogout);


        // Xử lý sự kiện click
        ivBack.setOnClickListener(v -> finish()); // Nút X -> Đóng Activity
        btnLogout.setOnClickListener(v -> logout());

        // Gọi API để lấp đầy dữ liệu
        fetchUserProfile();

        // Thiết lập dữ liệu tĩnh (từ ảnh mẫu)
        setupStaticData();
    }

    private void fetchUserProfile() {
        apiService.getMe().enqueue(new Callback<BaseResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserDTO>> call, Response<BaseResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserDTO user = response.body().getData();
                    // Lấp đầy dữ liệu từ API
                    populateUserData(user);
                } else if (response.code() == 401) {
                    // Token hết hạn
                    Toast.makeText(ProfileActivity.this, "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
                    logout();
                } else {
                    Toast.makeText(ProfileActivity.this, "Không thể lấy thông tin user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserDTO>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(UserDTO user) {
        // 1. Header
        tvName.setText(user.getName());
        // (UserDTO của bạn không có ngày tham gia, nên tôi sẽ ẩn nó)
        tvJoinDate.setVisibility(View.GONE);
        tvJoinDateValue.setVisibility(View.GONE);

        // 2. Mục Giấy phép lái xe
        // (UserDTO không có trạng thái, ta tự suy luận)
        if (user.getLicenseNumber() != null && !user.getLicenseNumber().isEmpty()) {
            setupItemRow(itemLicense, "Giấy phép lái xe", "Đã xác thực", user.getLicenseNumber(), true);
        } else {
            setupItemRow(itemLicense, "Giấy phép lái xe", "Chưa xác thực", "Xác thực ngay", false);
        }

        // 3. Mục Số điện thoại
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            setupItemRow(itemPhone, "Số điện thoại", "Đã xác thực", user.getPhone(), true);
        } else {
            setupItemRow(itemPhone, "Số điện thoại", "Chưa xác thực", "Thêm SĐT", false);
        }

        // 4. Mục Email
        // Email luôn có (vì dùng để đăng nhập)
        setupItemRow(itemEmail, "Email", "Đã xác thực", user.getEmail(), true);
    }

    private void setupStaticData() {
        // Thiết lập 2 mục Facebook và Google (không có trong API)
        setupItemRow(itemFacebook, "Facebook", "Chưa xác thực", "Liên kết ngay", false);
        setupItemRow(itemGoogle, "Google", "Chưa xác thực", "Liên kết ngay", false);
    }


    private void setupItemRow(View view, String title, String status, String actionOrValue, boolean isVerified) {
        TextView tvTitle = view.findViewById(R.id.tvItemTitle);
        TextView tvStatus = view.findViewById(R.id.tvItemStatus);
        TextView tvAction = view.findViewById(R.id.tvItemAction);

        tvTitle.setText(title);
        tvStatus.setText(status);
        tvAction.setText(actionOrValue);

        if (isVerified) {
            tvStatus.setBackgroundResource(R.drawable.shape_status_verified);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green_text));
        } else {
            tvStatus.setBackgroundResource(R.drawable.shape_status_pending);
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.status_orange_text));
        }
    }

    private void logout() {

        tokenManager.clearToken();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}