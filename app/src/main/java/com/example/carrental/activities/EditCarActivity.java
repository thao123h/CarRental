package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.FuelType;
import com.example.carrental.modals.enums.Transmission;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.modals.item.ItemImageDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCarActivity extends AppCompatActivity {

    // --- Biến lưu trữ ---
    private long carId; // ID của xe cần sửa

    // --- Thông tin Item ---
    private TextInputEditText edtName, edtPrice, edtDeposit, edtAddress, edtDescription;

    // --- Thông tin Car ---
    private TextInputEditText edtBrand, edtModel, edtYear, edtLicensePlate, edtSeats, edtCapacity;
    private AutoCompleteTextView spinnerTransmission, spinnerFuelType;

    // --- Hình ảnh và Nút bấm ---
    private ImageView imgPreview;
    private TextInputEditText edtImageUrl;
    private Button btnUpdateCar;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_car);

        // Lấy ID xe từ Intent
        carId = getIntent().getLongExtra("CAR_ID", -1);
        if (carId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin xe!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        setupSpinners();
        setupListeners();

        // Tải dữ liệu của xe lên các trường nhập liệu
        loadCarDetails();
    }

    private void initializeViews() {
        // Ánh xạ View cho Item
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDeposit = findViewById(R.id.edtDeposit);
        edtAddress = findViewById(R.id.edtAddress);
        edtDescription = findViewById(R.id.edtDescription);

        // Ánh xạ View cho Car
        edtBrand = findViewById(R.id.edtBrand);
        edtModel = findViewById(R.id.edtModel);
        edtYear = findViewById(R.id.edtYear);
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtSeats = findViewById(R.id.edtSeats);
        edtCapacity = findViewById(R.id.edtCapacity);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);

        // Ánh xạ View cho ảnh và nút
        imgPreview = findViewById(R.id.imgPreview);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        btnUpdateCar = findViewById(R.id.btnUpdateCar); // ID mới
        btnCancel = findViewById(R.id.btnCancel);

        // Thay đổi tiêu đề
        TextView title = findViewById(R.id.tvAddCarTitle);
        title.setText("Chỉnh Sửa Thông Tin Xe");
    }

    private void setupSpinners() {
        // Tương tự AddCarActivity
        String[] transmissions = {Transmission.MANUAL.name(), Transmission.AUTOMATIC.name()};
        spinnerTransmission.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, transmissions));
        String[] fuels = {FuelType.PETROL.name(), FuelType.DIESEL.name(), FuelType.ELECTRIC.name()};
        spinnerFuelType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuels));
    }

    private void setupListeners() {
        btnUpdateCar.setOnClickListener(v -> updateCar());
        btnCancel.setOnClickListener(v -> finish());

        // Xem trước ảnh
        edtImageUrl.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                updateImagePreview(s.toString().trim());
            }
        });
    }

    private void loadCarDetails() {
        ItemService itemService = RetrofitClient.createService(this, ItemService.class);

        // Gọi đúng phương thức `getItemById` và chuyển `carId` sang `Long`.
        itemService.getItemById(Long.valueOf(carId)).enqueue(new Callback<BaseResponse<ItemDTO>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponse<ItemDTO>> call, @NonNull Response<BaseResponse<ItemDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    populateFields(response.body().getData());
                } else {
                    Toast.makeText(EditCarActivity.this, "Không thể tải chi tiết xe.", Toast.LENGTH_SHORT).show();
                    Log.e("EditCar", "API Error: " + response.code());
                    finish(); // Đóng Activity nếu có lỗi
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseResponse<ItemDTO>> call, @NonNull Throwable t) {
                Toast.makeText(EditCarActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
                Log.e("EditCar", "API Failure: " + t.getMessage(), t);
                finish(); // Đóng Activity nếu có lỗi
            }
        });
    }

    private void populateFields(ItemDTO item) {
        CarDTO car = item.getCarDTO();
        if (car == null) return;

        // Điền thông tin Item
        edtName.setText(item.getName());
        edtPrice.setText(String.valueOf(item.getPrice()));
        edtDeposit.setText(String.valueOf(item.getDepositAmount()));
        edtAddress.setText(item.getAddress());
        edtDescription.setText(item.getDescription());

        // Điền thông tin Car
        edtBrand.setText(car.getBrand());
        edtModel.setText(car.getModel());
        edtYear.setText(String.valueOf(car.getYear()));
        edtLicensePlate.setText(car.getLicensePlate());
        edtSeats.setText(String.valueOf(car.getSeats()));
        // ✅ SỬA LỖI 1: Tạm thời bỏ qua trường Capacity nếu không có trong DTO
        // edtCapacity.setText(car.getCapacity() != null ? String.valueOf(car.getCapacity()) : "");

        // Cần setFilter(false) để có thể gán giá trị cho AutoCompleteTextView
        spinnerTransmission.setText(car.getTransmission().name(), false);
        spinnerFuelType.setText(car.getFuelType().name(), false);

        // Điền thông tin ảnh
        if (item.getItemImages() != null && !item.getItemImages().isEmpty()) {
            String imageUrl = item.getItemImages().get(0).getImageUrl();
            edtImageUrl.setText(imageUrl);
            updateImagePreview(imageUrl);
        } else {
            updateImagePreview("");
        }
    }

    private void updateImagePreview(String url) {
        if (url == null || url.isEmpty()) {
            imgPreview.setImageResource(R.drawable.placeholder);
        } else {
            Glide.with(this).load(url).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(imgPreview);
        }
    }

    private void updateCar() {
        try {
            // 1. Tạo đối tượng CarDTO
            CarDTO carDetails = new CarDTO();
            carDetails.setBrand(Objects.requireNonNull(edtBrand.getText()).toString().trim());
            carDetails.setModel(Objects.requireNonNull(edtModel.getText()).toString().trim());
            carDetails.setYear(Integer.parseInt(Objects.requireNonNull(edtYear.getText()).toString().trim()));
            carDetails.setLicensePlate(Objects.requireNonNull(edtLicensePlate.getText()).toString().trim());
            carDetails.setSeats(Integer.parseInt(Objects.requireNonNull(edtSeats.getText()).toString().trim()));
            carDetails.setTransmission(Transmission.valueOf(spinnerTransmission.getText().toString()));
            carDetails.setFuelType(FuelType.valueOf(spinnerFuelType.getText().toString()));

            List<ItemImageDTO> imageList = new ArrayList<>();
            String imageUrl = Objects.requireNonNull(edtImageUrl.getText()).toString().trim();
            if (!imageUrl.isEmpty()) {
                ItemImageDTO imageDTO = new ItemImageDTO();
                imageDTO.setImageUrl(imageUrl);
                imageList.add(imageDTO);
            }

            ItemDTO itemToUpdate = new ItemDTO();
            itemToUpdate.setId(Long.valueOf(carId)); // Quan trọng: Gắn ID để server biết cần cập nhật item nào
            itemToUpdate.setName(Objects.requireNonNull(edtName.getText()).toString().trim());
            itemToUpdate.setPrice(Double.parseDouble(Objects.requireNonNull(edtPrice.getText()).toString().trim()));
            itemToUpdate.setDepositAmount(Double.parseDouble(Objects.requireNonNull(edtDeposit.getText()).toString().trim()));
            itemToUpdate.setAddress(Objects.requireNonNull(edtAddress.getText()).toString().trim());
            itemToUpdate.setDescription(Objects.requireNonNull(edtDescription.getText()).toString().trim());
            carDetails.setItemImages(imageList);
            carDetails.setItem(itemToUpdate);



            // 4. Gọi API để cập nhật
            ItemService itemService = RetrofitClient.createService(this, ItemService.class);
            itemService.updateCar(Long.valueOf(carId),carDetails).enqueue(new Callback<BaseResponse>() {
                @Override
                public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditCarActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        // Gửi tín hiệu để CarListActivity tải lại dữ liệu
                        setResult(RESULT_OK);
                        Intent intent = new Intent(EditCarActivity.this, CarListActivity.class);
                        startActivity(intent);
                        finish(); // Đóng Activity
                    } else {
                        Toast.makeText(EditCarActivity.this, "Cập nhật thất bại.", Toast.LENGTH_SHORT).show();
                        Log.e("EditCar", "API Error: " + response.code() + " | Message: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse> call, Throwable t) {
                    Toast.makeText(EditCarActivity.this, "Lỗi kết nối.", Toast.LENGTH_SHORT).show();
                    Log.e("EditCar", "API Failure: " + t.getMessage(), t);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ và hợp lệ!", Toast.LENGTH_SHORT).show();
            Log.e("EditCar", "Lỗi khi thu thập dữ liệu: " + e.getMessage(), e);
        }
    }
}
