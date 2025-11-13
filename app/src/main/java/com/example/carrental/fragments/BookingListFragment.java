package com.example.carrental.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrental.R;
import com.example.carrental.activities.BookingDetailActivity;
import com.example.carrental.adapters.BookingAdapter;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.booking.BookingResponseDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.BookingService;
import com.example.carrental.utils.JwtDecoder;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị danh sách các đơn đặt xe (Booking)
 * Có 2 chế độ hiển thị: người thuê (RENTER) và chủ xe (OWNER)
 */
public class BookingListFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private static final String TAG = "BookingListFragment";

    public enum ViewMode {
        RENTER, OWNER
    }
     TokenManager tokenManager = null;

    // UI
    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private ImageView ivMenu;
    private RecyclerView rvBookings;
    private View emptyStateView;
    private ProgressBar progressBar;

    // Data
    private BookingAdapter adapter;
    private BookingService bookingService;
    private ViewMode currentViewMode = ViewMode.RENTER;


    // User info

    private boolean hasRenterRole = false;
    private boolean hasOwnerRole = false;

    public BookingListFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_booking_list, container, false);

        initViews(view);
        setupToolbar();
        setupRecyclerView();
        initRetrofit();
        initUserInfo();
        setupMenuButton();

        loadBookings();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle);
        ivMenu = view.findViewById(R.id.ivMenu);
        rvBookings = view.findViewById(R.id.rvBookings);
        emptyStateView = view.findViewById(R.id.emptyStateView);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        tvToolbarTitle.setText("");
        updateToolbarTitle();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupRecyclerView() {
        adapter = new BookingAdapter(requireContext(), this);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initRetrofit() {
        bookingService = RetrofitClient.createService(requireContext(), BookingService.class);
    }

    private void initUserInfo() {

        tokenManager = new TokenManager(requireContext());
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "No token found. Please login.", Toast.LENGTH_SHORT).show();
            return;
        }

        hasRenterRole = tokenManager.getRoles().contains("RENTER");
        hasOwnerRole = tokenManager.getRoles().contains("OWNER");

        Log.d(TAG, "Has RENTER role: " + hasRenterRole);
        Log.d(TAG, "Has OWNER role: " + hasOwnerRole);


        if (hasRenterRole) {
            currentViewMode = ViewMode.RENTER;
            Toast.makeText(requireContext(), "role renter", Toast.LENGTH_SHORT).show();
        } else if (hasOwnerRole) {
            currentViewMode = ViewMode.OWNER;
            Toast.makeText(requireContext(), "role owner", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No valid role found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!hasRenterRole || !hasOwnerRole) {
            ivMenu.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupMenuButton() {
        if (hasRenterRole && hasOwnerRole) {
            ivMenu.setOnClickListener(v -> showViewModeMenu());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showViewModeMenu() {
        PopupMenu popup = new PopupMenu(requireContext(), ivMenu);
        if (currentViewMode == ViewMode.RENTER) {
            popup.getMenu().add("Xem đơn cho thuê");
        } else {
            popup.getMenu().add("Xem chuyến của tôi");
        }

        popup.setOnMenuItemClickListener((MenuItem item) -> {
            toggleViewMode();
            return true;
        });

        popup.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void toggleViewMode() {
        if (currentViewMode == ViewMode.RENTER) {
            currentViewMode = ViewMode.OWNER;
        } else {
            currentViewMode = ViewMode.RENTER;
        }

        updateToolbarTitle();
        loadBookings();
    }

    private void updateToolbarTitle() {
        if (currentViewMode == ViewMode.RENTER) {
            tvToolbarTitle.setText("Chuyến của tôi");
        } else {
            tvToolbarTitle.setText("Đơn cho thuê");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadBookings() {
        showLoading();

        Call<BaseResponse<BookingResponseDTO[]>> call;

        if (currentViewMode == ViewMode.RENTER) {
            call = bookingService.getAllBookingsByRenter();
        } else {

            if(hasOwnerRole){
                call = bookingService.getAllBookingsByOwner();
                Log.d("role", "role owner");
                Toast.makeText(requireContext(), "role owner", Toast.LENGTH_SHORT).show();
            }
           else {
                call = bookingService.getAllBookingsByRenter();
                Log.d("role", "renter");
                Toast.makeText(requireContext(), "role renter", Toast.LENGTH_SHORT).show();
            }

        }

        call.enqueue(new Callback<BaseResponse<BookingResponseDTO[]>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<BaseResponse<BookingResponseDTO[]>> call,
                                   Response<BaseResponse<BookingResponseDTO[]>> response) {
                hideLoading();

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<BookingResponseDTO[]> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        List<BookingResponseDTO> bookings = Arrays.asList(baseResponse.getData());
                        if (bookings.isEmpty()) {
                            showEmptyState();
                        } else {
                            showBookingList(bookings);
                        }
                    } else {
                        showError(baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải danh sách đơn");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<BookingResponseDTO[]>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "Error loading bookings", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBookingList(List<BookingResponseDTO> bookings) {
        emptyStateView.setVisibility(View.GONE);
        rvBookings.setVisibility(View.VISIBLE);
        adapter.setBookingList(bookings);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showEmptyState() {
        rvBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        adapter.clearBookings();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        showEmptyState();
    }

    @Override
    public void onBookingClick(BookingResponseDTO booking) {
        Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
        intent.putExtra("BOOKING_ID", booking.getId());
        intent.putExtra("VIEW_MODE", currentViewMode.name());
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }
}
