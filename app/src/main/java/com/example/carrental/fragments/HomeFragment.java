package com.example.carrental.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.carrental.R;
import com.example.carrental.activities.ItemDetailActivity;
import com.example.carrental.adapters.ItemAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.Category;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.util.Log;

public class HomeFragment extends Fragment implements ItemAdapter.OnItemActionListener {
    private RecyclerView rv;
    private ItemAdapter adapter;
    private ItemService api;
    private EditText edtLocation;
    private SwipeRefreshLayout swipe;
    private Button btnFind;
    private List<ItemDTO> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout
        View view = inflater.inflate(R.layout.home, container, false);

        // Khởi tạo các view
        api = RetrofitClient.createService(requireContext(), ItemService.class);
        rv = view.findViewById(R.id.rvCars);
        swipe = view.findViewById(R.id.swipeRefresh);
        btnFind = view.findViewById(R.id.btnFind);
        edtLocation = view.findViewById(R.id.edtLocation);

        // RecyclerView setup
        adapter = new ItemAdapter(requireContext(), items, this);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        swipe.setOnRefreshListener(this::loadData);
        edtLocation.setOnClickListener(v -> {
            edtLocation.setFocusable(true);
            edtLocation.setFocusableInTouchMode(true);
            edtLocation.requestFocus();
        });

        // Gọi loadData khi khởi tạo fragment
        loadData();

        btnFind.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null && requireActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
            }

            String address = edtLocation.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(requireContext(), "Address cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            List<ItemDTO> filteredList = items.stream()
                    .filter(item -> item.getAddress() != null &&
                            item.getAddress().toLowerCase().contains(address.toLowerCase()))
                    .collect(Collectors.toList());
            adapter.setData(filteredList);
        });

        return view;
    }

    private void loadData() {
        swipe.setRefreshing(true);

        api.getAllByCategory(Category.CAR).enqueue(new Callback<BaseResponse<List<ItemDTO>>>() {
            @Override
            public void onResponse(Call<BaseResponse<List<ItemDTO>>> call, Response<BaseResponse<List<ItemDTO>>> response) {
                swipe.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    items = response.body().getData();
                    Log.d("HomeFragment", "Số lượng item trả về: " + (items != null ? items.size() : 0));
                    for (int i = 0; i < items.size(); i++) {
                        ItemDTO item = items.get(i);
                        Log.d("HomeFragment", String.format(
                                "Item %d: id=%s, name=%s, price=%.0f, address=%s, images=%s",
                                i + 1,
                                item.getId(),
                                item.getName(),
                                item.getPrice(),
                                item.getAddress(),
                                (item.getItemImages() != null && !item.getItemImages().isEmpty())
                                        ? item.getItemImages().get(0).getImageUrl()
                                        : "no image"
                        ));
                    }

                    adapter.setData(items);
                } else {
                    Toast.makeText(requireContext(), "Load error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<List<ItemDTO>>> call, Throwable t) {
                swipe.setRefreshing(false);
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCarImageClick(ItemDTO car) {
        Intent intent = new Intent(requireContext(), ItemDetailActivity.class);
        intent.putExtra("item_id", car.getId());
        startActivity(intent);
    }
}
