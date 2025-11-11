package com.example.carrental.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrental.R;
import com.example.carrental.adapters.CarAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarListActivity extends AppCompatActivity {

    private RecyclerView rvCarList;
    private CarAdapter carAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        rvCarList = findViewById(R.id.rvCarList);
        rvCarList.setLayoutManager(new LinearLayoutManager(this));

        Log.d("CarList", "onCreate gọi loadCarList()");
        loadCarList();
    }

    private void loadCarList() {
        ItemService itemService = RetrofitClient.createService(this, ItemService.class);
        Log.d("CarList", "Gọi API getAllCars()");
        itemService.getAllCars().enqueue(new Callback<BaseResponse<List<CarDTO>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<CarDTO>>> call, Response<BaseResponse<List<CarDTO>>> response) {
                Log.d("CarList", "Đã nhận response: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<CarDTO> carList = response.body().getData();
                    Log.d("CarList", "Số lượng xe: " + carList.size());
                    Log.d("CarList", "Dữ liệu: " + new Gson().toJson(carList));

                    if (carList.isEmpty()) {
                        Toast.makeText(CarListActivity.this, "Không có xe nào để hiển thị", Toast.LENGTH_SHORT).show();
                    }

                    carAdapter = new CarAdapter(CarListActivity.this, carList);
                    rvCarList.setAdapter(carAdapter);
                } else {
                    Log.e("CarList", "Response không hợp lệ hoặc data null");
                    Toast.makeText(CarListActivity.this, "Không thể tải danh sách xe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<CarDTO>>> call, Throwable t) {
                Log.e("CarList", "API lỗi: " + t.getMessage());
                Toast.makeText(CarListActivity.this, "Lỗi kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
