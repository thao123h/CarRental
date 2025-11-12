package com.example.carrental.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị danh sách các đơn đặt xe (Booking)
 *
 * BEHAVIOR:
 * - Both RENTER and OWNER views are accessible to ALL users
 * - Menu is ALWAYS visible
 * - If user has no data for a view → Shows empty state (not error)
 * - Empty state allows navigation to home
 */
public class BookingListFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private static final String TAG = "BookingListFragment";

    public enum ViewMode {
        RENTER, OWNER
    }

    // UI
    private Toolbar toolbar;
    private TextView tvToolbarTitle;
    private ImageView ivMenu;
    private RecyclerView rvBookings;
    private View emptyStateView;
    private ProgressBar progressBar;
    private TextView tvEmptyMessage;
    private Button btnGoHome;

    // Data
    private BookingAdapter adapter;
    private BookingService bookingService;
    private ViewMode currentViewMode = ViewMode.RENTER;
    private TokenManager tokenManager;

    // User info
    private Long userId;

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
        setupEmptyStateButton();

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

        // Empty state views
        tvEmptyMessage = emptyStateView.findViewById(R.id.tvEmptyMessage);
        btnGoHome = emptyStateView.findViewById(R.id.btnGoHome);
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

        userId = JwtDecoder.getUserId(token);

        Log.d(TAG, "User ID: " + userId);

        // Default to RENTER view
        currentViewMode = ViewMode.RENTER;

        // ALWAYS show menu - users can switch between views
        ivMenu.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupMenuButton() {
        ivMenu.setOnClickListener(v -> showViewModeMenu());
    }

    private void setupEmptyStateButton() {
        if (btnGoHome != null) {
            btnGoHome.setOnClickListener(v -> navigateToHome());
        }
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
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

        Log.d(TAG, "=== LOADING BOOKINGS ===");
        Log.d(TAG, "Current ViewMode: " + currentViewMode);
        Log.d(TAG, "User ID: " + userId);

        Call<BaseResponse<BookingResponseDTO[]>> call;

        if (currentViewMode == ViewMode.RENTER) {
            Log.d(TAG, "Calling: GET /bookings/renter");
            call = bookingService.getAllBookingsByRenter();
        } else {
            // OWNER view
            if (userId == null) {
                Log.e(TAG, "ERROR: userId is NULL!");
                // Show empty state instead of error
                hideLoading();
                showEmptyStateWithMessage("Không thể tải danh sách. Vui lòng đăng nhập lại.");
                return;
            }
            Log.d(TAG, "Calling: GET /bookings/owner/" + userId);
            call = bookingService.getAllBookingsByOwnerId(userId);
        }

        call.enqueue(new Callback<BaseResponse<BookingResponseDTO[]>>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<BaseResponse<BookingResponseDTO[]>> call,
                                   Response<BaseResponse<BookingResponseDTO[]>> response) {
                hideLoading();

                Log.d(TAG, "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<BookingResponseDTO[]> baseResponse = response.body();

                    Log.d(TAG, "BaseResponse success: " + baseResponse.isSuccess());

                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        // Create MUTABLE ArrayList (not fixed-size Arrays.asList)
                        List<BookingResponseDTO> bookings = new ArrayList<>(Arrays.asList(baseResponse.getData()));
                        Log.d(TAG, "Number of bookings: " + bookings.size());

                        if (bookings.isEmpty()) {
                            // User has no data for this view - show friendly empty state
                            showEmptyState();
                        } else {
                            showBookingList(bookings);
                        }
                    } else {
                        // Backend returned success=false
                        Log.w(TAG, "Backend returned error: " + baseResponse.getMessage());
                        // Still show empty state, not error
                        showEmptyState();
                    }
                } else {
                    // HTTP error (400, 404, 500, etc.)
                    Log.e(TAG, "HTTP Error: " + response.code());

                    // For 404 or similar, user probably has no data - show empty state
                    if (response.code() == 404 || response.code() == 400) {
                        Log.d(TAG, "User has no data for this view - showing empty state");
                        showEmptyState();
                    } else {
                        // Real error - show error message
                        showEmptyStateWithMessage("Lỗi tải dữ liệu (HTTP " + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<BookingResponseDTO[]>> call, Throwable t) {
                hideLoading();
                Log.e(TAG, "API call FAILED!", t);

                // Network error - show error in empty state
                showEmptyStateWithMessage("Lỗi kết nối mạng");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showBookingList(List<BookingResponseDTO> bookings) {
        emptyStateView.setVisibility(View.GONE);
        rvBookings.setVisibility(View.VISIBLE);
        adapter.setBookingList(bookings);
    }

    /**
     * Show empty state with default message based on view mode
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showEmptyState() {
        rvBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        adapter.clearBookings();

        // Set message based on current view mode
        if (tvEmptyMessage != null) {
            if (currentViewMode == ViewMode.RENTER) {
                tvEmptyMessage.setText("Bạn chưa đặt xe nào");
            } else {
                // OWNER view - user might not have posted any cars yet
                tvEmptyMessage.setText("Bạn chưa có đơn cho thuê nào.\nĐăng xe của bạn để bắt đầu cho thuê!");
            }
        }
    }

    /**
     * Show empty state with custom error message
     */
    private void showEmptyStateWithMessage(String message) {
        rvBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
        adapter.clearBookings();

        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(message);
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        rvBookings.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
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