package com.example.carrental.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carrental.R;
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
 * Activity to display booking list
 * Supports two view modes: RENTER (my bookings) and OWNER (booking requests)
 * Automatically detects user roles from JWT token
 */
public class BookingListActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private static final String TAG = "BookingListActivity";

    // View Mode
    public enum ViewMode {
        RENTER,  // Show bookings where user is the renter
        OWNER    // Show bookings on user's cars (owner view)
    }

    // UI Components
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
    private TokenManager tokenManager;

    // User info from JWT
    private Long userId;
    private boolean hasRenterRole = false;
    private boolean hasOwnerRole = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);

        initViews();
        setupToolbar();
        setupRecyclerView();
        initRetrofit();
        initUserInfo();
        setupMenuButton();

        // Load initial data (RENTER view by default if user has RENTER role)
        loadBookings();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle);
        ivMenu = findViewById(R.id.ivMenu);
        rvBookings = findViewById(R.id.rvBookings);
        emptyStateView = findViewById(R.id.emptyStateView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        updateToolbarTitle();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupRecyclerView() {
        adapter = new BookingAdapter(this, this);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initRetrofit() {
        bookingService = RetrofitClient.createService(this, BookingService.class);
    }

    /**
     * Initialize user info from JWT token
     */
    private void initUserInfo() {
        tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token found. Please login.", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to login screen
            finish();
            return;
        }

        // Decode JWT token to get user info
        userId = JwtDecoder.getUserId(token);
        hasRenterRole = JwtDecoder.hasRenterRole(token);
        hasOwnerRole = JwtDecoder.hasOwnerRole(token);

        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "Has RENTER role: " + hasRenterRole);
        Log.d(TAG, "Has OWNER role: " + hasOwnerRole);

        // Determine initial view mode
        if (hasRenterRole) {
            currentViewMode = ViewMode.RENTER;
        } else if (hasOwnerRole) {
            currentViewMode = ViewMode.OWNER;
        } else {
            Toast.makeText(this, "No valid role found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Hide menu button if user only has one role
        if (!hasRenterRole || !hasOwnerRole) {
            ivMenu.setVisibility(View.GONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupMenuButton() {
        // Only show menu if user has both roles
        if (hasRenterRole && hasOwnerRole) {
            ivMenu.setOnClickListener(v -> showViewModeMenu());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showViewModeMenu() {
        PopupMenu popup = new PopupMenu(this, ivMenu);

        if (currentViewMode == ViewMode.RENTER) {
            popup.getMenu().add("Xem đơn cho thuê");
        } else {
            popup.getMenu().add("Xem chuyến của tôi");
        }

        popup.setOnMenuItemClickListener(item -> {
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
            // Get bookings where user is renter
            call = bookingService.getAllBookingsByRenter();
        } else {
            // Get bookings where user is owner (use userId from token)
            if (userId == null) {
                showError("Cannot load owner bookings: User ID not found");
                return;
            }
            call = bookingService.getAllBookingsByOwnerId(userId);
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
                        BookingResponseDTO[] bookingsArray = baseResponse.getData();
                        List<BookingResponseDTO> bookingsList = Arrays.asList(bookingsArray);

                        if (bookingsList.isEmpty()) {
                            showEmptyState();
                        } else {
                            showBookingList(bookingsList);
                        }
                    } else {
                        showError(baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải danh sách đơn");
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
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
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showEmptyState();
    }

    @Override
    public void onBookingClick(BookingResponseDTO booking) {
        // Navigate to booking detail
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra("BOOKING_ID", booking.getId());
        intent.putExtra("VIEW_MODE", currentViewMode.name());
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        // Reload bookings when returning to this activity
        loadBookings();
    }
}