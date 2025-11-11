package com.example.carrental.activities;




import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carrental.R;
import com.example.carrental.adapters.ItemAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.Category;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ItemAdapter.OnItemActionListener {
    private RecyclerView rv;
    private ItemAdapter adapter;

    private ItemService api;
    EditText edtLocation;
    private SwipeRefreshLayout swipe;
    Button btnFind;
    List<ItemDTO> items = new ArrayList<>();
    private static final int REQ_ADD = 1001;
    private static final int REQ_EDIT = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        api = RetrofitClient.createService(this, ItemService.class);
        rv = findViewById(R.id.rvCars);
        swipe = findViewById(R.id.swipeRefresh);
        btnFind = findViewById(R.id.btnFind);
        edtLocation = findViewById(R.id.edtLocation);
        adapter = new ItemAdapter(this, items, this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadData);
        edtLocation.setOnClickListener(v -> {
            edtLocation.setFocusable(true);
            edtLocation.setFocusableInTouchMode(true);
            edtLocation.requestFocus();
        });
        loadData();
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                String address = edtLocation.getText().toString().trim();
                if(address == null || address.isEmpty()){
                    Toast.makeText(HomeActivity.this, "Address can not empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                List<ItemDTO> filteredList = items.stream()
                        .filter(item -> item.getAddress() != null
                                && item.getAddress().toLowerCase().contains(address.toLowerCase()))
                        .collect(Collectors.toList());
                adapter.setData(filteredList);
            }
        });
    }

    private void loadData() {
        swipe.setRefreshing(true);

        api.getAllByCategory(Category.CAR).enqueue(new Callback<BaseResponse<List<ItemDTO>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ItemDTO>>> call, Response<BaseResponse<List<ItemDTO>>> response) {
                swipe.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Lấy danh sách thực bên trong BaseResponse
                    items = response.body().getData();

                    adapter.setData(items);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQ_ADD || requestCode == REQ_EDIT) && resultCode == RESULT_OK) {
            loadData();
        }
    }

    @Override
    public void onCarImageClick(ItemDTO car) {
        Intent intent = new Intent(this, ItemDetailActivity.class);
        intent.putExtra("item_id", car.getId());
        startActivity(intent);
    }
}
