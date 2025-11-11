package com.example.carrental.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.FuelType;
import com.example.carrental.modals.enums.Transmission;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.modals.item.ItemDTO; // ✅ Đã import ItemDTO
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCarActivity extends AppCompatActivity {

    private TextInputEditText edtBrand, edtModel, edtYear, edtLicensePlate, edtSeats, edtCapacity;
    private AutoCompleteTextView spinnerTransmission, spinnerFuelType;
    private Button btnAddCar;
    private Button btnCancel; // ✅ Thêm nút Cancel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        // Ánh xạ view
        edtBrand = findViewById(R.id.edtBrand);
        edtModel = findViewById(R.id.edtModel);
        edtYear = findViewById(R.id.edtYear);
        edtLicensePlate = findViewById(R.id.edtLicensePlate);
        edtSeats = findViewById(R.id.edtSeats);
        edtCapacity = findViewById(R.id.edtCapacity);
        spinnerTransmission = findViewById(R.id.spinnerTransmission);
        spinnerFuelType = findViewById(R.id.spinnerFuelType);
        btnAddCar = findViewById(R.id.btnAddCar);
        btnCancel = findViewById(R.id.btnCancel); // ✅ Ánh xạ nút Cancel

        // Set dữ liệu cho dropdown Hộp số
        String[] transmissions = {
                Transmission.MANUAL.name(),
                Transmission.AUTOMATIC.name()
        };
        spinnerTransmission.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, transmissions));

        // Set dữ liệu cho dropdown Nhiên liệu
        String[] fuels = {
                FuelType.PETROL.name(),
                FuelType.DIESEL.name(),
                FuelType.ELECTRIC.name()
        };
        spinnerFuelType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, fuels));

        // Xử lý nút Thêm xe
        btnAddCar.setOnClickListener(v -> addCar());

        // ✅ Xử lý nút Hủy
        btnCancel.setOnClickListener(v -> finish());
    }

    private void addCar() {
        try {
            CarDTO car = new CarDTO();
            car.setBrand(edtBrand.getText().toString().trim());
            car.setModel(edtModel.getText().toString().trim());
            car.setYear(Integer.parseInt(edtYear.getText().toString().trim()));
            car.setLicensePlate(edtLicensePlate.getText().toString().trim());
            car.setSeats(Integer.parseInt(edtSeats.getText().toString().trim()));
            // Giả sử bạn muốn set giá trị từ edtCapacity. Backend cần xử lý trường này.
            // Ví dụ: car.setKms(Integer.parseInt(edtCapacity.getText().toString().trim()));

            car.setTransmission(Transmission.valueOf(spinnerTransmission.getText().toString()));
            car.setFuelType(FuelType.valueOf(spinnerFuelType.getText().toString()));

            ItemService itemService = RetrofitClient.createService(this, ItemService.class);

            // ✅ SỬA LỖI: Dùng đúng kiểu ItemDTO trong Callback để khớp với ItemService
            itemService.createCar(car).enqueue(new Callback<BaseResponse<ItemDTO>>() {
                @Override
                public void onResponse(Call<BaseResponse<ItemDTO>> call, Response<BaseResponse<ItemDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Toast.makeText(AddCarActivity.this, "Thêm xe thành công!", Toast.LENGTH_SHORT).show();

                        // Lấy ItemDTO trả về để có thể lấy ID nếu cần
                        ItemDTO newItem = response.body().getData();
                        Log.d("AddCar", "Đã tạo item mới với ID: " + newItem.getId());

                        // TODO: Chuyển sang màn hình upload ảnh với ID của item

                        finish(); // Đóng Activity và quay lại danh sách
                    } else {
                        // Cải thiện hiển thị lỗi chi tiết từ server
                        String errorMessage = "Thêm xe thất bại!";
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
            Toast.makeText(this, "Năm sản xuất và Số ghế phải là số!", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Vui lòng chọn giá trị hợp lệ cho Hộp số và Nhiên liệu!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("AddCar", "Lỗi không xác định: " + e.getMessage(), e);
            Toast.makeText(this, "Vui lòng nhập đầy đủ và hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }
}
