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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.Category;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCarActivity extends AppCompatActivity {

    // --- Thông tin Item ---
    private TextInputEditText edtName, edtPrice, edtDeposit, edtAddress, edtDescription;

    // --- Thông tin Car ---
    private TextInputEditText edtBrand, edtModel, edtYear, edtLicensePlate, edtSeats, edtCapacity;
    private AutoCompleteTextView spinnerTransmission, spinnerFuelType;

    // --- Hình ảnh và Nút bấm ---
    private ImageView imgPreview;
    private TextInputEditText edtImageUrl;
    private Button btnAddCar;
    private Button btnCancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        // --- Ánh xạ View cho Item ---
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDeposit = findViewById(R.id.edtDeposit);
        edtAddress = findViewById(R.id.edtAddress);
        edtDescription = findViewById(R.id.edtDescription);

        // --- Ánh xạ View cho Car ---
        edtBrand = findViewById(R.id.edtBrand);
        edtModel = findViewById(R.id.edtModel);
        edtYear = findViewById(R.id.edtYear);
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtSeats = findViewById(R.id.edtSeats);
        edtCapacity = findViewById(R.id.edtCapacity);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);

        // --- Ánh xạ View cho ảnh và nút ---
        imgPreview = findViewById(R.id.imgPreview);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        btnAddCar = findViewById(R.id.btnAddCar);
        btnCancel = findViewById(R.id.btnCancel);
        setupSpinners();
        setupListeners();
    }

    private void setupSpinners() {
        // Set dữ liệu cho dropdown Hộp số
        String[] transmissions = {Transmission.MANUAL.name(), Transmission.AUTOMATIC.name()};
        spinnerTransmission.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, transmissions));

        // Set dữ liệu cho dropdown Nhiên liệu
        String[] fuels = {FuelType.PETROL.name(), FuelType.DIESEL.name(), FuelType.ELECTRIC.name()};
        spinnerFuelType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuels));
    }

    private void setupListeners() {
        // Xử lý nút Đăng tin
        btnAddCar.setOnClickListener(v -> addCar());

        // Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> finish());

        // Xem trước ảnh khi URL thay đổi
        edtImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateImagePreview(s.toString().trim());
            }
        });
        updateImagePreview(""); // Khởi tạo ảnh placeholder
    }

    private void updateImagePreview(String url) {
        if (url == null || url.isEmpty()) {
            imgPreview.setImageResource(R.drawable.placeholder);
        } else {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(imgPreview);
        }
    }

    private void addCar() {
        try {
            ItemDTO newItemToPost = new ItemDTO();
            newItemToPost.setName(edtName.getText().toString().trim());
            newItemToPost.setPrice(Double.parseDouble(edtPrice.getText().toString().trim()));
            newItemToPost.setDepositAmount(Double.parseDouble(edtDeposit.getText().toString().trim()));
            newItemToPost.setAddress(edtAddress.getText().toString().trim());
            newItemToPost.setDescription(edtDescription.getText().toString().trim());
            newItemToPost.setCategory(Category.CAR);

            List<ItemImageDTO> imageList = new ArrayList<>();
            String imageUrl = edtImageUrl.getText().toString().trim();
            if (!imageUrl.isEmpty()) {
                ItemImageDTO imageDTO = new ItemImageDTO();
                imageDTO.setImageUrl(imageUrl);
                imageList.add(imageDTO);
            }

            // 1. Tạo đối tượng CarDTO (thông tin chi tiết xe)
            CarDTO carDetails = new CarDTO();
            carDetails.setBrand(edtBrand.getText().toString().trim());
            carDetails.setModel(edtModel.getText().toString().trim());
            carDetails.setYear(Integer.parseInt(edtYear.getText().toString().trim()));
            carDetails.setLicensePlate(edtLicensePlate.getText().toString().trim());
            carDetails.setSeats(Integer.parseInt(edtSeats.getText().toString().trim()));
            carDetails.setTransmission(Transmission.valueOf(spinnerTransmission.getText().toString()));
            carDetails.setFuelType(FuelType.valueOf(spinnerFuelType.getText().toString()));
            carDetails.setItem(newItemToPost);
            carDetails.setItemImages(imageList);

            // 🧾 LOG TOÀN BỘ GIÁ TRỊ
            Log.d("AddCar", "=== ITEM INFO ===");
            Log.d("AddCar", "Name: " + newItemToPost.getName());
            Log.d("AddCar", "Price: " + newItemToPost.getPrice());
            Log.d("AddCar", "Deposit: " + newItemToPost.getDepositAmount());
            Log.d("AddCar", "Address: " + newItemToPost.getAddress());
            Log.d("AddCar", "Description: " + newItemToPost.getDescription());

            Log.d("AddCar", "=== CAR DETAILS ===");
            Log.d("AddCar", "Brand: " + carDetails.getBrand());
            Log.d("AddCar", "Model: " + carDetails.getModel());
            Log.d("AddCar", "Year: " + carDetails.getYear());
            Log.d("AddCar", "License Plate: " + carDetails.getLicensePlate());
            Log.d("AddCar", "Seats: " + carDetails.getSeats());
            Log.d("AddCar", "Transmission: " + carDetails.getTransmission());
            Log.d("AddCar", "Fuel Type: " + carDetails.getFuelType());

            if (!imageList.isEmpty()) {
                for (ItemImageDTO img : imageList) {
                    Log.d("AddCar", "Image URL: " + img.getImageUrl());
                }
            } else {
                Log.d("AddCar", "No images provided.");
            }


            // 4. Gọi API để gửi ItemDTO lên server
            ItemService itemService = RetrofitClient.createService(this, ItemService.class);
            // Giả sử API của bạn là createItem(ItemDTO), nếu tên khác hãy sửa lại
            itemService.createCar(carDetails).enqueue(new Callback<BaseResponse<ItemDTO>>() {
                @Override
                public void onResponse(Call<BaseResponse<ItemDTO>> call, Response<BaseResponse<ItemDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AddCarActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                        ItemDTO createdItem = response.body().getData();
                        Log.d("AddCar", "Đã tạo item mới với ID: " + createdItem.getId());
                        Intent intent = new Intent(AddCarActivity.this, CarListActivity.class);
                        startActivity(intent);
                        finish(); // nếu bạn muốn đóng Activity hiện tại

//                        finish(); // Đóng Activity và quay lại danh sách
                    } else {
                        String errorMessage = "Đăng tin thất bại!";
                        if (response.errorBody() != null) {
                            try {
                                errorMessage = response.errorBody().string();
                            } catch (Exception e) {
                                Log.e("AddCar", "Lỗi đọc errorBody", e);
                            }
                        }
                        Log.e("AddCar", "API Error: " + response.code() + " - " + errorMessage);
                        Toast.makeText(AddCarActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<ItemDTO>> call, Throwable t) {
                    Log.e("AddCar", "Lỗi khi gọi API: " + t.getMessage(), t);
                    Toast.makeText(AddCarActivity.this, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng số!", Toast.LENGTH_SHORT).show();
            Log.e("AddCar", "Lỗi định dạng số", e);
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Vui lòng chọn giá trị hợp lệ!", Toast.LENGTH_SHORT).show();
            Log.e("AddCar", "Lỗi giá trị không hợp lệ (enum)", e);
        } catch (Exception e) {
            Log.e("AddCar", "Lỗi không xác định: " + e.getMessage(), e);
            Toast.makeText(this, "Vui lòng nhập đầy đủ và hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }
}
