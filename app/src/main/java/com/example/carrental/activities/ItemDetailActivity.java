package com.example.carrental.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ItemDetailActivity extends AppCompatActivity {

    private TextView tvItemName, tvAvailability, tvPrice, tvDeposit, tvDescription, tvAddress, tvCategory;
    private TextView tvTotalDays, tvEstimatedTotal;
    private Button btnStartDate, btnEndDate, btnConfirm;
    private ItemService api;
    private Calendar startDate, endDate;
    private double pricePerDay = 0.0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_details);
        api = RetrofitClient.createService(this, ItemService.class);
        initViews();

        // Nhận ID từ Intent
        long itemId = getIntent().getLongExtra("itemId", -1);
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
        tvAddress = findViewById(R.id.tv_address);
        tvCategory = findViewById(R.id.tv_category);
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
                    ItemDTO item = response.body().getData();
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
        tvPrice.setText("₫ " + moneyFormat.format(item.getItemValue()));
        tvDeposit.setText("₫ " + moneyFormat.format(item.getDepositAmount()));
        tvDescription.setText(item.getDescription());
        tvAddress.setText("Location: " + item.getAddress());
        tvCategory.setText("Category: " + item.getCategory());

        pricePerDay = item.getPrice();
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

                    if (isStartDate) {
                        startDate = selected;
                        btnStartDate.setText("Start: " + dateFormat.format(selected.getTime()));
                    } else {
                        endDate = selected;
                        btnEndDate.setText("End: " + dateFormat.format(selected.getTime()));
                    }

                    calculateTotal();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void calculateTotal() {
        if (startDate != null && endDate != null && !endDate.before(startDate)) {
            long diffMillis = endDate.getTimeInMillis() - startDate.getTimeInMillis();
            int days = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
            tvTotalDays.setText("Days: " + days);
            tvEstimatedTotal.setText("Total: ₫ " + moneyFormat.format(days * pricePerDay));
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
            Toast.makeText(this, "Booking confirmed!", Toast.LENGTH_SHORT).show();
        });
    }
}
