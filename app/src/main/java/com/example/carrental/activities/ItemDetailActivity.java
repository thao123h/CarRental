package com.example.carrental.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.booking.ScheduleDTO;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView tvItemName, tvAvailability, tvPrice, tvDeposit, tvDescription, tvAddress, tvCategory;
    private TextView tvTotalDays, tvEstimatedTotal;
    private Button btnStartDate, btnEndDate, btnConfirm;
    private ImageView imgItem;
    private ItemService api;
    private Calendar startDate, endDate;
    private double pricePerDay = 0.0;
    private List<ScheduleDTO> scheduleList = new ArrayList<>();
    private ItemDTO item = null;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);
        api = RetrofitClient.createService(this, ItemService.class);
        initViews();

        // Nhận ID từ Intent
        long itemId = getIntent().getLongExtra("item_id", -1);
        if (itemId != -1) {
            loadItemDetail(itemId);
        } else {
            Toast.makeText(this, "Invalid item ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupDatePickers();
        setupConfirmButton();
    }

    private void initViews() {
        tvItemName = findViewById(R.id.tv_item_name);
        tvAvailability = findViewById(R.id.tv_availability);
        tvPrice = findViewById(R.id.tv_price);
        tvDeposit = findViewById(R.id.tv_deposit);
        tvDescription = findViewById(R.id.tv_description);
        imgItem = findViewById(R.id.img_item);
        tvAddress = findViewById(R.id.tv_address);
        tvTotalDays = findViewById(R.id.tv_total_days);
        tvEstimatedTotal = findViewById(R.id.tv_estimated_total);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);
        btnConfirm = findViewById(R.id.btn_confirm);
    }

    private void loadItemDetail(long id) {
      api.getItemById(id).enqueue(new Callback<BaseResponse<ItemDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<ItemDTO>> call, Response<BaseResponse<ItemDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    item = response.body().getData();
                    bindData(item);
                } else {
                    Toast.makeText(ItemDetailActivity.this, "No data found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<ItemDTO>> call, Throwable t) {
                Toast.makeText(ItemDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void bindData(ItemDTO item) {
        tvItemName.setText(item.getName());
        tvPrice.setText( String.format("%,.0fđ/ngày",item.getPrice()));
        tvDeposit.setText( String.format("%,.0fđ", item.getDepositAmount()));
        tvDescription.setText(item.getDescription());
        tvAddress.setText("Địa điểm " + item.getAddress());
        pricePerDay = item.getPrice();
        if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
            Glide.with(this)
                    .load(item.getItemImages().get(0).getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imgItem);
        }
    }

    private void setupDatePickers() {
        btnStartDate.setOnClickListener(v -> showDatePicker(true));
        btnEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);

                    Calendar today = Calendar.getInstance();
                    today.set(Calendar.HOUR_OF_DAY, 0);
                    today.set(Calendar.MINUTE, 0);
                    today.set(Calendar.SECOND, 0);

                    //  Không cho chọn ngày trước hôm nay
                    if (selected.before(today)) {
                        Toast.makeText(this, "Không thể chọn ngày trong quá khứ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //  Nếu là EndDate và nhỏ hơn StartDate → báo lỗi
                    if (!isStartDate && startDate != null && selected.before(startDate)) {
                        Toast.makeText(this, "Ngày kết thúc phải sau ngày bắt đầu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //  Nếu ngày nằm trong khoảng bị đặt (ScheduleDTO) → báo lỗi
                    if (isInBookedDate(selected)) {
                        Toast.makeText(this, "Ngày này đã được đặt, vui lòng chọn ngày khác", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //  Nếu hợp lệ → lưu lại
                    if (isStartDate) {
                        startDate = selected;
                        btnStartDate.setText("Bắt đầu: " + dateFormat.format(selected.getTime()));
                    } else {
                        endDate = selected;
                        btnEndDate.setText("Kết thúc: " + dateFormat.format(selected.getTime()));
                    }

                    calculateTotal();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // ⚙️ Giới hạn: không cho chọn ngày trước hôm nay
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        dialog.show();
    }
    private boolean isInBookedDate(Calendar selected) {
        if (scheduleList == null || scheduleList.isEmpty()) return false;

        for (ScheduleDTO s : scheduleList) {
            LocalDateTime start = s.getStartTime();
            LocalDateTime end = s.getEndTime();

            // convert sang Calendar để so sánh
            Calendar startCal = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startCal.set(start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth(), 0, 0, 0);
            }

            Calendar endCal = Calendar.getInstance();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                endCal.set(end.getYear(), end.getMonthValue() - 1, end.getDayOfMonth(), 23, 59, 59);
            }

            if (!selected.before(startCal) && !selected.after(endCal)) {
                return true; // nằm trong khoảng bị book
            }
        }
        return false;
    }


    @SuppressLint("DefaultLocale")
    private void calculateTotal() {
        if (startDate != null && endDate != null && !endDate.before(startDate)) {
            long diffMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
            int days = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
            tvTotalDays.setText("Days: " + days);
            tvEstimatedTotal.setText(String.format("%,.0fđ", pricePerDay*days));
        } else {
            tvTotalDays.setText("Days: 0");
            tvEstimatedTotal.setText("Total: 0đ");
        }
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> {
            if (startDate == null || endDate == null) {
                Toast.makeText(this, "Please select both start and end dates", Toast.LENGTH_SHORT).show();
                return;
            }

            String startDateStr = dateFormat.format(startDate.getTime());
            String endDateStr = dateFormat.format(endDate.getTime());
            String totalPrice = tvEstimatedTotal.getText().toString();
            // Tạo Intent gửi sang BookingDetailsActivity
            Intent intent = new Intent(ItemDetailActivity.this, BookingDetailsActivity.class);
            intent.putExtra("car_id", item.getId());
            intent.putExtra("car_name", item.getName());
            intent.putExtra("address", item.getAddress());
            intent.putExtra("start_date", startDateStr );
            intent.putExtra("end_date", endDateStr);
            intent.putExtra("total_price", totalPrice);

            if (item.getItemImages().get(0).getImageUrl() != null) {
                intent.putExtra("car_image_url", item.getItemImages().get(0).getImageUrl() );
            }

            startActivity(intent);
        });
    }
}
