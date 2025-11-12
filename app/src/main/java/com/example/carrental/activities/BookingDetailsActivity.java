package com.example.carrental.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.booking.BookingRequestDTO;
import com.example.carrental.modals.booking.BookingResponseDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.BookingService;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailsActivity extends AppCompatActivity {

    BookingService api ;
    private ImageView imgCar;
    private TextView tvCarName, tvAddress, tvStartDate, tvEndDate, tvTotalPrice;
    private TextView tvRenterName, tvRenterEmail;
    private Button btnConfirmBooking;

    private TokenManager tokenManager;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_details);
        api = RetrofitClient.createService(this, BookingService.class);// đổi tên XML nếu cần

        tokenManager = new TokenManager(this);

        initViews();
        loadIntentData();
        loadRenterInfo();
        setupConfirmButton();
    }

    private void initViews() {
        imgCar = findViewById(R.id.img_car);
        tvCarName = findViewById(R.id.tv_car_name);
        tvAddress = findViewById(R.id.tv_address);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        tvRenterName = findViewById(R.id.tv_renter_name);
        tvRenterEmail = findViewById(R.id.tv_renter_email);

        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
    }

    private void loadIntentData() {
        if (getIntent() != null) {

            String carName = getIntent().getStringExtra("car_name");
            String address = getIntent().getStringExtra("address");
            String startDate = getIntent().getStringExtra("start_date");
            String endDate = getIntent().getStringExtra("end_date");
            String totalPrice = getIntent().getStringExtra("total_price");
            String carImageUrl = getIntent().getStringExtra("car_image_url");

            tvCarName.setText(carName != null ? carName : "N/A");
            tvAddress.setText(address != null ? address : "N/A");
            tvStartDate.setText(startDate != null ? "Ngày bắt đầu thuê: " + startDate : "Ngày bắt đầu thuê: N/A");
            tvEndDate.setText(endDate != null ? "Ngày kết thúc thuê: " + endDate : "Ngày kết thúc thuê: N/A");
            tvTotalPrice.setText(totalPrice != null ? "Tổng tiền thuê: " + totalPrice : "Tổng tiền thuê: N/A");

            if (carImageUrl != null && !carImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(carImageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(imgCar);
            }
        }
    }

    private void loadRenterInfo() {
        String renterName = tokenManager.getUsername();
        String renterEmail = tokenManager.getEmail();

        tvRenterName.setText(renterName != null ? renterName : "N/A");
        tvRenterEmail.setText(renterEmail != null ? renterEmail : "N/A");
    }

    private void setupConfirmButton() {
        btnConfirmBooking.setOnClickListener(v -> {
            // Lấy ID xe từ Intent
            Long itemId = getIntent().getLongExtra("car_id", 0);
            if (itemId == 0) {
                Toast.makeText(this, "Car ID không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy ngày bắt đầu và kết thúc từ TextView
            String startDateStr = getIntent().getStringExtra("start_date"); // ví dụ "11/11/2025"
            String endDateStr = getIntent().getStringExtra("end_date");     // ví dụ "12/11/2025"

            if (startDateStr == null || endDateStr == null) {
                Toast.makeText(this, "Ngày bắt đầu hoặc kết thúc chưa hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                String[] startParts = startDateStr.split("/");
                String[] endParts = endDateStr.split("/");

                String startTime = startParts[2] + "-" + startParts[1] + "-" + startParts[0] + "T09:00:00";
                String endTime = endParts[2] + "-" + endParts[1] + "-" + endParts[0] + "T18:00:00";

                // Log ra để kiểm tra
                System.out.println("Booking JSON gửi đi:");
                System.out.println("{\"itemId\":" + itemId + ", \"startTime\":\"" + startTime + "\", \"endTime\":\"" + endTime + "\"}");

                BookingRequestDTO bookingRequest = new BookingRequestDTO(itemId, startTime, endTime);
                // Gửi request lên API
                api.createBooking(bookingRequest).enqueue(new Callback<BaseResponse<BookingResponseDTO>>() {
                    @Override
                    public void onResponse(Call<BaseResponse<BookingResponseDTO>> call, Response<BaseResponse<BookingResponseDTO>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(BookingDetailsActivity.this, "Booking request sent successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(BookingDetailsActivity.this, MainActivity.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(BookingDetailsActivity.this, "Booking failed: " + response.code(), Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onFailure(Call<BaseResponse<BookingResponseDTO>> call, Throwable t) {
                        Toast.makeText(BookingDetailsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, "Lỗi khi chuyển ngày: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
