package com.example.carrental.activities;




import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carrental.R;
import com.example.carrental.adapters.ItemAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.Category;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ItemAdapter.OnItemActionListener {
    private RecyclerView rv;
    private ItemAdapter adapter;
    private List<ItemDTO> list = new ArrayList<>();
    private ItemService api;
    private SwipeRefreshLayout swipe;

    private static final int REQ_ADD = 1001;
    private static final int REQ_EDIT = 1002;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        api = RetrofitClient.createService(this, ItemService.class);
        rv = findViewById(R.id.rvCars);
        swipe = findViewById(R.id.swipeRefresh);
        adapter = new ItemAdapter(this, list, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadData);

        Button btnMyBookings = findViewById(R.id.btnMyBookings);

        btnMyBookings.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BookingListActivity.class);
            startActivity(intent);
        });

        loadData();
    }

    private void loadData() {
        swipe.setRefreshing(true);

        api.getAllByCategory(Category.CAR).enqueue(new Callback<BaseResponse<List<ItemDTO>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ItemDTO>>> call, Response<BaseResponse<List<ItemDTO>>> response) {
                swipe.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Lấy danh sách thực bên trong BaseResponse
                    List<ItemDTO> list = response.body().getData();
                    adapter.setData(list);
                } else {
                    Toast.makeText(HomeActivity.this, "Load error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ItemDTO>>> call, Throwable t) {
                swipe.setRefreshing(false);
                Toast.makeText(HomeActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

//
//    @Override
//    public void onEdit(Product p) {
//        Intent i = new Intent(this, AddEditActivity.class);
//        i.putExtra(AddEditActivity.EXTRA_PRODUCT, p);
//        startActivityForResult(i, REQ_EDIT);
//    }
//
//    @Override
//    public void onDelete(Product p) {
//        new AlertDialog.Builder(this)
//                .setTitle("Xóa")
//                .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
//                .setPositiveButton("Có", (d, w) -> {
//                    api.delete(p.getId()).enqueue(new Callback<Void>() {
//                        @Override public void onResponse(Call<Void> call, Response<Void> response) {
//                            if (response.isSuccessful()) {
//                                Toast.makeText(MainActivity.this, "Đã xóa", Toast.LENGTH_SHORT).show();
//                                loadData();
//                            } else {
//                                Toast.makeText(MainActivity.this, "Lỗi xóa: " + response.code(), Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        @Override public void onFailure(Call<Void> call, Throwable t) {
//                            Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                })
//                .setNegativeButton("Không", null)
//                .show();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQ_ADD || requestCode == REQ_EDIT) && resultCode == RESULT_OK) {
            loadData();
        }
    }
}
