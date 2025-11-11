package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
    private UserDTO currentUser;
    private TokenManager tokenManager;

    // Các View trong Profile Header
    private ImageView ivBack, ivAvatar, ivEdit, ivShare;
    private TextView tvName, tvJoinDate , tvJoinDateValue;


    // Các View cho từng mục
    private View itemLicense, itemPhone, itemEmail, cccd, itemGoogle;
    private Button btnLogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Khởi tạo
        apiService = RetrofitClient.createService(this, AuthApiService.class);
        tokenManager = new TokenManager(this);

        // Ánh xạ (giữ nguyên)
        ivBack = findViewById(R.id.ivBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivEdit = findViewById(R.id.ivEdit);
        ivShare = findViewById(R.id.ivShare);
        tvName = findViewById(R.id.tvName);
        tvJoinDateValue = findViewById(R.id.tvJoinDateValue);
        tvJoinDate = findViewById(R.id.tvJoinDate);
        itemLicense = findViewById(R.id.itemLicense); // <-- Đã có
        itemPhone = findViewById(R.id.itemPhone);
        itemEmail = findViewById(R.id.itemEmail);
        cccd = findViewById(R.id.cccd); // <-- Đã có
        itemGoogle = findViewById(R.id.itemGoogle);
        btnLogout = findViewById(R.id.btnLogout);


        // --- Xử lý sự kiện click ---
        ivBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> logout());

        // Sửa Tên
        ivEdit.setOnClickListener(v -> {
            showEditNameDialog();
        });

        // Sửa Email
        itemEmail.setOnClickListener(v -> {
            showEditEmailDialog();
        });

        // !!! THÊM SỰ KIỆN CLICK CHO GPLX VÀ CCCD !!!
        itemLicense.setOnClickListener(v -> {
            showEditLicenseDialog();
        });

        cccd.setOnClickListener(v -> {
            showEditCccdDialog();
        });

        itemPhone.setOnClickListener(v -> {
            showEditPhoneDialog();
        });

        // Gọi API
        fetchUserProfile();

        // Thiết lập dữ liệu tĩnh (chỉ còn Google)
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
        this.currentUser = user;
        // 1. Header
        tvName.setText(user.getName());
        tvJoinDate.setVisibility(View.GONE);
        tvJoinDateValue.setVisibility(View.GONE);

        // 2. Mục Giấy phép lái xe
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
        setupItemRow(itemEmail, "Email", "Đã xác thực", user.getEmail(), true);

        // 5. MỤC CCCD (Chuyển từ setupStaticData lên đây)
        if (user.getIdentityCard() != null && !user.getIdentityCard().isEmpty()) {
            setupItemRow(cccd, "CCCD", "Đã xác thực", user.getIdentityCard(), true);
        } else {
            setupItemRow(cccd, "CCCD", "Chưa xác thực", "Xác thực ngay", false);
        }
    }

    private void setupStaticData() {
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

    private void saveProfileChanges(String newName) {
        // Kiểm tra xem đã fetch được user chưa
        if (currentUser == null) {
            Toast.makeText(this, "Thông tin user chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(newName);

        callUpdateApi();
    }

    private void showEditNameDialog() {
        // Kiểm tra xem đã tải xong thông tin user chưa
        if (currentUser == null) {
            Toast.makeText(this, "Chưa tải được thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo hộp thoại
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi tên của bạn");

        // 2. Tạo một EditText để người dùng nhập liệu
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        // Đặt tên hiện tại của user vào EditText
        input.setText(currentUser.getName());

        // Thêm EditText vào một FrameLayout để có padding đẹp
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        // Set margin (khoảng cách lề) cho EditText
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);

        builder.setView(container);

        // 3. Thiết lập nút "Lưu"
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();

            // Chỉ gọi API nếu tên mới hợp lệ và khác tên cũ
            if (!newName.isEmpty() && !newName.equals(currentUser.getName())) {
                // Gọi hàm saveProfileChanges bạn đã viết!
                saveProfileChanges(newName);
            }
        });

        // 4. Thiết lập nút "Hủy"
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        // 5. Hiển thị hộp thoại
        builder.show();
    }

    private void showEditEmailDialog() {
        // Kiểm tra xem đã tải xong thông tin user chưa
        if (currentUser == null) {
            Toast.makeText(this, "Chưa tải được thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Tạo hộp thoại
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi email"); // <-- Sửa title

        // 2. Tạo một EditText để người dùng nhập liệu
        final EditText input = new EditText(this);
        // Sửa kiểu nhập liệu
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        // Đặt email hiện tại của user vào EditText
        input.setText(currentUser.getEmail()); // <-- Sửa getName thành getEmail

        // (Code FrameLayout giữ nguyên)
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        // 3. Thiết lập nút "Lưu"
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newEmail = input.getText().toString().trim(); // <-- Đây là email mới

            // Chỉ gọi API nếu email mới hợp lệ và khác email cũ
            if (!newEmail.isEmpty() && !newEmail.equals(currentUser.getEmail())) {
                // Gọi hàm saveProfileEmail (Hàm mới ở bước 3)
                saveProfileEmail(newEmail);
            }
        });

        // 4. Thiết lập nút "Hủy"
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        // 5. Hiển thị hộp thoại
        builder.show();
    }

    // -----------------------------------------------------------------
    // !!! BƯỚC 3: TẠO HÀM MỚI ĐỂ LƯU EMAIL (Copy từ hàm lưu Tên) !!!
    // -----------------------------------------------------------------

    private void saveProfileEmail(String newEmail) {
        // Kiểm tra xem đã fetch được user chưa
        if (currentUser == null) {
            Toast.makeText(this, "Thông tin user chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Cập nhật đối tượng 'currentUser' với giá trị mới
        currentUser.setEmail(newEmail);

        callUpdateApi();
    }

    private void showEditLicenseDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "Chưa tải được thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi số GPLX"); // <-- Sửa title

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT); // <-- Sửa kiểu nhập (GPLX có thể có chữ)
        input.setText(currentUser.getLicenseNumber()); // <-- Sửa getter

        FrameLayout container = new FrameLayout(this);
        // ... (code FrameLayout giữ nguyên)
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newLicense = input.getText().toString().trim(); // <-- Đây là GPLX mới
            if (!newLicense.isEmpty() && !newLicense.equals(currentUser.getLicenseNumber())) {
                saveProfileLicense(newLicense); // <-- Gọi hàm save mới
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProfileLicense(String newLicense) {
        if (currentUser == null) {
            Toast.makeText(this, "Thông tin user chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setLicenseNumber(newLicense); // <-- Sửa setter

        // Gọi hàm cập nhật API (dùng chung)
        callUpdateApi();
    }

    private void showEditCccdDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "Chưa tải được thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi số CCCD"); // <-- Sửa title

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER); // <-- Sửa kiểu nhập (CCCD là số)
        input.setText(currentUser.getIdentityCard()); // <-- Sửa getter

        FrameLayout container = new FrameLayout(this);
        // ... (code FrameLayout giữ nguyên)
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newCccd = input.getText().toString().trim(); // <-- Đây là CCCD mới
            if (!newCccd.isEmpty() && !newCccd.equals(currentUser.getIdentityCard())) {
                saveProfileCccd(newCccd); // <-- Gọi hàm save mới
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProfileCccd(String newCccd) {
        if (currentUser == null) {
            Toast.makeText(this, "Thông tin user chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setIdentityCard(newCccd); // <-- Sửa setter

        // Gọi hàm cập nhật API (dùng chung)
        callUpdateApi();
    }

    private void callUpdateApi() {
        if (currentUser == null) {
            Toast.makeText(this, "Lỗi: Dữ liệu người dùng bị rỗng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi *toàn bộ* đối tượng DTO (đã được cập nhật) lên server
        apiService.updateMe(currentUser).enqueue(new Callback<BaseResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserDTO>> call, Response<BaseResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Lấy dữ liệu mới nhất và điền lại lên UI
                    populateUserData(response.body().getData());
                } else {
                    Toast.makeText(ProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserDTO>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditPhoneDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "Chưa tải được thông tin, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thay đổi số điện thoại"); // <-- Sửa title

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE); // <-- Sửa kiểu nhập
        input.setText(currentUser.getPhone()); // <-- Sửa getter

        FrameLayout container = new FrameLayout(this);
        // ... (code FrameLayout giữ nguyên)
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newPhone = input.getText().toString().trim(); // <-- Đây là SĐT mới
            if (!newPhone.isEmpty() && !newPhone.equals(currentUser.getPhone())) {
                saveProfilePhone(newPhone); // <-- Gọi hàm save mới
            }
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveProfilePhone(String newPhone) {
        if (currentUser == null) {
            Toast.makeText(this, "Thông tin user chưa được tải", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setPhone(newPhone); // <-- Sửa setter

        // Gọi hàm cập nhật API (dùng chung)
        callUpdateApi();
    }

    private void logout() {

        tokenManager.clearToken();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}