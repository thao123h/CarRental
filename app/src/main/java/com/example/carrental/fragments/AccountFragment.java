package com.example.carrental.fragments; // Thay bằng package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

// Import các class của bạn
import com.example.carrental.R;
import com.example.carrental.activities.AddCarActivity;
import com.example.carrental.activities.CarListActivity;
import com.example.carrental.activities.ProfileActivity;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.AuthApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private AuthApiService apiService;

    // Header
    private CardView cardProfileHeader;
    private TextView tvName;
    private ImageView ivAvatar;

    // Các mục
    private View itemRegisterCar, itemFavoriteCars, itemPersonalInfo;
    private TokenManager token = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout cho Fragment này
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ApiService
        // Dùng getActivity() để lấy Context
        apiService = RetrofitClient.createService(getActivity(), AuthApiService.class);
       token =  new TokenManager(requireContext());
        // Ánh xạ Header
        cardProfileHeader = view.findViewById(R.id.cardProfileHeader);
        tvName = view.findViewById(R.id.tvName);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        // Ánh xạ 3 mục
        itemRegisterCar = view.findViewById(R.id.itemRegisterCar);
        itemFavoriteCars = view.findViewById(R.id.itemFavoriteCars);
        itemPersonalInfo = view.findViewById(R.id.itemPersonalInfo);

        // --- Cài đặt dữ liệu cho 3 mục ---
        // (Bạn phải dùng hàm setupItem)
        if(token.getRoles().contains("RENTER")){
            setupItem(itemRegisterCar, "Đăng ký cho thuê xe", R.drawable.ic_document);

        }
        else if (token.getRoles().contains("OWNER")){
            setupItem(itemRegisterCar, "Xe của tôi", R.drawable.ic_document);
            Intent intent = new Intent(requireContext(), CarListActivity.class);
        }

        setupItem(itemFavoriteCars, "Xe yêu thích", R.drawable.ic_heart);
        setupItem(itemPersonalInfo, "Thông tin cá nhân", R.drawable.ic_profile);

        // --- Xử lý Click ---

        // Mục "Thông tin cá nhân" -> Mở ProfileActivity
        itemPersonalInfo.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Click vào Header (Thảo) -> Cũng mở ProfileActivity
        cardProfileHeader.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            startActivity(intent);
        });

        // Click vào 2 mục còn lại (hiện tại chỉ báo Toast)
        itemRegisterCar.setOnClickListener(v -> {

            if(token.getRoles().contains("OWNER")){
                Intent intent = new Intent(requireContext(), CarListActivity.class);
                startActivity(intent);
            }
            else{
                Intent intent = new Intent(requireContext(), AddCarActivity.class);
            startActivity(intent);}
        });


        itemFavoriteCars.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Mở màn hình Xe yêu thích", Toast.LENGTH_SHORT).show();
        });

        // Gọi API để lấy tên User (ví dụ: "Thảo")
        fetchUserName();
    }

    /**
     * Hàm trợ giúp để cài đặt icon và tiêu đề cho một mục
     */
    private void setupItem(View itemView, String title, int iconResId) {
        TextView tvTitle = itemView.findViewById(R.id.tvTitle);
        ImageView ivIcon = itemView.findViewById(R.id.ivIcon);

        tvTitle.setText(title);
        ivIcon.setImageResource(iconResId);
    }

    /**
     * Gọi API /getMe để lấy tên và hiển thị
     */
    private void fetchUserName() {
        apiService.getMe().enqueue(new Callback<BaseResponse<UserDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<UserDTO>> call, Response<BaseResponse<UserDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserDTO user = response.body().getData();
                    tvName.setText(user.getName());
                    // (Bạn cũng có thể dùng Glide/Picasso để tải avatar ở đây)
                } else {
                    // Nếu lỗi (ví dụ token hết hạn)
                    tvName.setText("Khách");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<UserDTO>> call, Throwable t) {
                tvName.setText("Khách");
            }
        });
    }

    // Khi quay lại tab này (ví dụ: sau khi cập nhật tên ở ProfileActivity),
    // hãy gọi API để làm mới tên
    @Override
    public void onResume() {
        super.onResume();
        if (apiService != null) {
            fetchUserName();
        }
    }
}