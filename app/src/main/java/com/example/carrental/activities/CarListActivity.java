package com.example.carrental.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // Thêm import này
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrental.R;
import com.example.carrental.adapters.CarAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarListActivity extends AppCompatActivity {

    private RecyclerView rvCarList;
    private CarAdapter carAdapter;

    // ✅ Thêm hằng số để xử lý kết quả trả về từ EditCarActivity và AddCarActivity
    private static final int EDIT_CAR_REQUEST_CODE = 101;
    private static final int ADD_CAR_REQUEST_CODE = 102;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);

        rvCarList = findViewById(R.id.rvCarList);
        rvCarList.setLayoutManager(new LinearLayoutManager(this));

        // Đăng ký RecyclerView để nó có thể hiển thị context menu
        registerForContextMenu(rvCarList);

        Log.d("CarList", "onCreate gọi loadCarList()");
        loadCarList();

        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(CarListActivity.this, AddCarActivityD.class);
            // ✅ Sử dụng startActivityForResult để có thể tải lại danh sách sau khi thêm thành công
            startActivityForResult(intent, ADD_CAR_REQUEST_CODE);
        });

    }

    private void loadCarList() {
        ItemService itemService = RetrofitClient.createService(this, ItemService.class);
        Log.d("CarList", "Gọi API getAllCars()");
        itemService.getAllMyItems().enqueue(new Callback<BaseResponse<List<ItemDTO>>>() {

            @Override
            public void onResponse(Call<BaseResponse<List<ItemDTO>>> call, Response<BaseResponse<List<ItemDTO>>> response) {
                Log.d("CarList", "Đã nhận response: " + response.code());

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<ItemDTO> itemList = response.body().getData();
                    Log.d("CarList", "Số lượng xe: " + itemList.size());

                    if (itemList.isEmpty()) {
                        Toast.makeText(CarListActivity.this, "Không có xe nào để hiển thị", Toast.LENGTH_SHORT).show();
                    }

                    carAdapter = new CarAdapter(CarListActivity.this, itemList);
                    rvCarList.setAdapter(carAdapter);  // ✅ Thêm dòng này
                } else {
                    Toast.makeText(CarListActivity.this, "Không tải được danh sách xe", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(@NonNull Call<BaseResponse<List<ItemDTO>>> call, @NonNull Throwable t) {
                Log.e("CarList", "API lỗi: " + t.getMessage());
                Toast.makeText(CarListActivity.this, "Lỗi kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // --- Bắt đầu phần code mới cho Context Menu ---

    // Ghi đè phương thức này để tạo menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Kiểm tra xem carAdapter đã được khởi tạo chưa
        if (carAdapter != null) {
            MenuInflater inflater = getMenuInflater();
            // Sử dụng một file menu resource để tạo menu
            inflater.inflate(R.menu.car_list_context_menu, menu);
        }
    }

    // Ghi đè phương thức này để xử lý sự kiện chọn item trong menu
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = carAdapter.getLongPressedPosition();
        if (position == -1 || position >= carAdapter.getItemCount()) {
            return super.onContextItemSelected(item);
        }

        ItemDTO selectedCar = carAdapter.getCarList().get(position);

        int itemId = item.getItemId();
        if (itemId == R.id.menu_edit) {
            Log.d("CarList", "Chọn sửa xe với ID: " + selectedCar.getId());
            Intent editIntent = new Intent(this, EditCarActivity.class);
            editIntent.putExtra("CAR_ID", selectedCar.getId());
            // ✅ Sử dụng startActivityForResult để có thể tải lại danh sách sau khi sửa
            startActivityForResult(editIntent, EDIT_CAR_REQUEST_CODE);
            return true;

        } else if (itemId == R.id.menu_delete) {
            // ✅ Hiển thị dialog xác nhận trước khi xóa
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa xe '" + selectedCar.getCarDTO().getBrand() + " " + selectedCar.getCarDTO().getModel() + "' không?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        // Người dùng nhấn "Xóa", gọi phương thức để thực hiện xóa
                        deleteCar(selectedCar, position);
                    })
                    .setNegativeButton("Hủy", null) // Không làm gì khi nhấn "Hủy"
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    // ✅ Thêm phương thức deleteCar
    private void deleteCar(ItemDTO carToDelete, int position) {
        if (carToDelete.getId() == null) {
            Toast.makeText(this, "ID của xe không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long carId = Long.valueOf(carToDelete.getId());
        Log.d("CarList", "Tiến hành xóa xe ID: " + carId);

        ItemService itemService = RetrofitClient.createService(this, ItemService.class);
        itemService.deleteItem(carId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                // Mã 200-299 được xem là thành công
                if (response.isSuccessful()) {
                    Toast.makeText(CarListActivity.this, "Đã xóa xe thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật giao diện RecyclerView
                    carAdapter.getCarList().remove(position);
                    carAdapter.notifyItemRemoved(position);
                    carAdapter.notifyItemRangeChanged(position, carAdapter.getItemCount());
                } else {
                    Log.e("CarList", "Lỗi khi xóa. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(CarListActivity.this, "Xóa xe thất bại. Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("CarList", "API Failure khi xóa: " + t.getMessage(), t);
                Toast.makeText(CarListActivity.this, "Lỗi kết nối khi xóa xe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Thêm phương thức onActivityResult để nhận kết quả và tải lại danh sách
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Kiểm tra nếu kết quả trả về là OK từ màn hình Sửa hoặc Thêm
        if (resultCode == RESULT_OK && (requestCode == EDIT_CAR_REQUEST_CODE || requestCode == ADD_CAR_REQUEST_CODE)) {
            Log.d("CarList", "Nhận được kết quả OK. Tải lại danh sách xe...");
            loadCarList(); // Tải lại toàn bộ danh sách để có dữ liệu mới nhất
        }
    }
}
