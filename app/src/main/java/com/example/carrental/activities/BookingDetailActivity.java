package com.example.carrental.activities;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.booking.BookingResponseDTO;
import com.example.carrental.modals.booking.UpdateBookingRequest;
import com.example.carrental.modals.enums.Status;
import com.example.carrental.modals.item.ItemImageDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.TokenManager;
import com.example.carrental.network.api.BookingService;
import com.example.carrental.utils.JwtDecoder;
import com.example.carrental.utils.StatusHelper;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity to display booking details
 * Shows different action buttons based on user's ACTUAL role in THIS booking
 *
 * FIXED: Now detects if current user is OWNER or RENTER of THIS specific booking
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class BookingDetailActivity extends AppCompatActivity {

    private static final String TAG = "BookingDetailActivity";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // UI Components
    private Toolbar toolbar;
    private ImageView ivCarBanner;
    private TextView tvCarName;
    private TextView tvLicensePlate;
    private TextView tvPrice;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private TextView tvAddress;
    private TextView tvRenterName;
    private TextView tvRenterPhone;
    private TextView tvOwnerName;
    private TextView tvOwnerPhone;
    private TextView tvStatus;
    private Button btnAccept;
    private Button btnReject;
    private Button btnCancel;

    // Data
    private BookingService bookingService;
    private BookingResponseDTO currentBooking;
    private Long bookingId;
    private String viewMode; // From intent - which list user came from

    // NEW: Current user info
    private Long currentUserId;
    private boolean isCurrentUserRenter = false;
    private boolean isCurrentUserOwner = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        initViews();
        setupToolbar();
        initRetrofit();
        initCurrentUser(); // NEW: Get current user info
        getIntentData();
        loadBookingDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivCarBanner = findViewById(R.id.ivCarBanner);
        tvCarName = findViewById(R.id.tvCarName);
        tvLicensePlate = findViewById(R.id.tvLicensePlate);
        tvPrice = findViewById(R.id.tvPrice);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvAddress = findViewById(R.id.tvAddress);
        tvRenterName = findViewById(R.id.tvRenterName);
        tvRenterPhone = findViewById(R.id.tvRenterPhone);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerPhone = findViewById(R.id.tvOwnerPhone);
        tvStatus = findViewById(R.id.tvStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initRetrofit() {
        bookingService = RetrofitClient.createService(this, BookingService.class);
    }

    /**
     * NEW: Get current user ID from token
     */
    private void initCurrentUser() {
        TokenManager tokenManager = new TokenManager(this);
        String token = tokenManager.getToken();

        if (token != null && !token.isEmpty()) {
            currentUserId = JwtDecoder.getUserId(token);
            Log.d(TAG, "Current User ID: " + currentUserId);
        } else {
            Log.e(TAG, "No token found!");
        }
    }

    private void getIntentData() {
        bookingId = getIntent().getLongExtra("BOOKING_ID", -1);
        viewMode = getIntent().getStringExtra("VIEW_MODE");

        if (bookingId == -1) {
            Toast.makeText(this, "Invalid booking ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadBookingDetail() {
        bookingService.getBookingById(bookingId).enqueue(new Callback<BaseResponse<BookingResponseDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<BookingResponseDTO>> call,
                                   Response<BaseResponse<BookingResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<BookingResponseDTO> baseResponse = response.body();
                    if (baseResponse.isSuccess() && baseResponse.getData() != null) {
                        currentBooking = baseResponse.getData();

                        // NEW: Determine user's role in THIS booking
                        determineUserRole();

                        displayBookingDetails();
                    } else {
                        showError(baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải chi tiết đơn");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<BookingResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Error loading booking detail", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * NEW: Determine if current user is RENTER or OWNER of this specific booking
     */
    private void determineUserRole() {
        if (currentBooking == null || currentUserId == null) {
            Log.w(TAG, "Cannot determine role: booking or userId is null");
            return;
        }

        // Check if current user is the RENTER
        if (currentBooking.getRenter() != null &&
                currentBooking.getRenter().getId() != null) {
            Long renterId = currentBooking.getRenter().getId();
            isCurrentUserRenter = renterId.equals(currentUserId);
            Log.d(TAG, "Is current user RENTER? " + isCurrentUserRenter +
                    " (RenterID: " + renterId + ", CurrentUserID: " + currentUserId + ")");
        }

        // Check if current user is the OWNER
        if (currentBooking.getItem() != null &&
                currentBooking.getItem().getOwner() != null &&
                currentBooking.getItem().getOwner().getId() != null) {
            Long ownerId = currentBooking.getItem().getOwner().getId();
            isCurrentUserOwner = ownerId.equals(currentUserId);
            Log.d(TAG, "Is current user OWNER? " + isCurrentUserOwner +
                    " (OwnerID: " + ownerId + ", CurrentUserID: " + currentUserId + ")");
        }
    }

    private void displayBookingDetails() {
        if (currentBooking == null) return;

        // Load car image
        loadCarImage();

        // Set car name
        if (currentBooking.getItem() != null) {
            String carName = currentBooking.getItem().getName();
            tvCarName.setText(carName != null ? carName : "N/A");

            // Set license plate - FIXED
            String licensePlate = "N/A";
            if (currentBooking.getItem().getCarDTO() != null) {
                String plate = currentBooking.getItem().getCarDTO().getLicensePlate();
                if (plate != null && !plate.trim().isEmpty()) {
                    licensePlate = plate;
                }
            }
            tvLicensePlate.setText(licensePlate);

            // Set price
            Double price = currentBooking.getItem().getPrice();
            if (price != null) {
                String formattedPrice = NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(price);
                tvPrice.setText("• Đơn giá: " + formattedPrice + " VNĐ / Ngày");
            }

            // Set address
            String address = currentBooking.getItem().getAddress();
            tvAddress.setText("• Địa điểm: " + (address != null ? address : "N/A"));
        }

        // Set dates
        try {
            String startDate = currentBooking.getStartTime();
            String endDate = currentBooking.getEndTime();

            tvStartDate.setText("• Ngày thuê: " + startDate);
            tvEndDate.setText("• Ngày trả: " + endDate);
        } catch (Exception e) {
            tvStartDate.setText("• Ngày thuê: N/A");
            tvEndDate.setText("• Ngày trả: N/A");
        }

        // Set renter info
        if (currentBooking.getRenter() != null) {
            String renterName = currentBooking.getRenter().getName();
            String renterPhone = currentBooking.getRenter().getPhone();
            tvRenterName.setText("• Người thuê: " + (renterName != null ? renterName : "N/A"));
            tvRenterPhone.setText("• SĐT người thuê: " + (renterPhone != null ? renterPhone : "N/A"));
        }

        // Set owner info
        if (currentBooking.getItem() != null && currentBooking.getItem().getOwner() != null) {
            String ownerName = currentBooking.getItem().getOwner().getName();
            String ownerPhone = currentBooking.getItem().getOwner().getPhone();
            tvOwnerName.setText("• Chủ xe: " + (ownerName != null ? ownerName : "N/A"));
            tvOwnerPhone.setText("• SĐT Chủ xe: " + (ownerPhone != null ? ownerPhone : "N/A"));
        }

        // Set status badge
        setStatusBadge();

        // Setup action buttons based on ACTUAL role
        setupActionButtons();
    }

    private void loadCarImage() {
        String imageUrl = null;

        if (currentBooking.getItem() != null) {
            // Try itemImages first
            if (currentBooking.getItem().getItemImages() != null &&
                    !currentBooking.getItem().getItemImages().isEmpty()) {
                ItemImageDTO firstImage = currentBooking.getItem().getItemImages().get(0);
                imageUrl = firstImage.getImageUrl();
            }
            // Try carDTO images
            else if (currentBooking.getItem().getCarDTO() != null &&
                    currentBooking.getItem().getCarDTO().getItemImages() != null &&
                    !currentBooking.getItem().getCarDTO().getItemImages().isEmpty()) {
                ItemImageDTO firstImage = currentBooking.getItem().getCarDTO().getItemImages().get(0);
                imageUrl = firstImage.getImageUrl();
            }
        }

        // Load with Glide
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder_car)
                    .error(R.drawable.ic_placeholder_car)
                    .centerCrop()
                    .into(ivCarBanner);
        } else {
            ivCarBanner.setImageResource(R.drawable.ic_placeholder_car);
        }
    }

    private void setStatusBadge() {
        String statusText = StatusHelper.getStatusText(currentBooking.getStatus());
        tvStatus.setText(statusText);

        int statusColor = StatusHelper.getStatusColor(currentBooking.getStatus());
        GradientDrawable drawable = (GradientDrawable) tvStatus.getBackground();
        if (drawable != null) {
            drawable.setColor(statusColor);
        }
    }

    /**
     * FIXED: Setup buttons based on user's ACTUAL role in THIS booking
     */
    private void setupActionButtons() {
        btnAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        Status status = currentBooking.getStatus();

        // NEW: Check ACTUAL role, not just viewMode
        if (isCurrentUserOwner) {
            // User IS the owner of this car
            if (StatusHelper.canOwnerRespond(status)) {
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);

                btnAccept.setOnClickListener(v -> showAcceptDialog());
                btnReject.setOnClickListener(v -> showRejectDialog());

                Log.d(TAG, "Showing OWNER buttons (Accept/Reject)");
            }
        } else if (isCurrentUserRenter) {
            // User IS the renter of this booking
            if (StatusHelper.canRenterCancel(status)) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> showCancelDialog());

                Log.d(TAG, "Showing RENTER button (Cancel)");
            }
        } else {
            // User is neither owner nor renter - show no buttons
            Log.d(TAG, "User is neither OWNER nor RENTER of this booking - no action buttons");
        }
    }

    private void showAcceptDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chấp thuận đơn")
                .setMessage("Bạn có chắc chắn muốn chấp thuận đơn này?")
                .setPositiveButton("Có", (dialog, which) -> acceptBooking())
                .setNegativeButton("Không", null)
                .show();
    }

    private void showRejectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Từ chối đơn");

        final EditText input = new EditText(this);
        input.setHint("Lý do từ chối");
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập lý do từ chối", Toast.LENGTH_SHORT).show();
            } else {
                rejectBooking(reason);
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hủy đơn");

        final EditText input = new EditText(this);
        input.setHint("Lý do hủy (không bắt buộc)");
        builder.setView(input);

        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            cancelBooking(reason.isEmpty() ? null : reason);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void acceptBooking() {
        UpdateBookingRequest request = new UpdateBookingRequest(Status.CONFIRMED, null, null);
        updateBooking(request, "Đã chấp thuận đơn thành công");
    }

    private void rejectBooking(String reason) {
        UpdateBookingRequest request = new UpdateBookingRequest(Status.CANCELLED, reason, null);
        updateBooking(request, "Đã từ chối đơn thành công");
    }

    private void cancelBooking(String reason) {
        UpdateBookingRequest request = new UpdateBookingRequest(Status.CANCELLED, reason, null);
        updateBooking(request, "Đã hủy đơn thành công");
    }

    private void updateBooking(UpdateBookingRequest request, String successMessage) {
        bookingService.updateBooking(bookingId, request).enqueue(new Callback<BaseResponse<BookingResponseDTO>>() {
            @Override
            public void onResponse(Call<BaseResponse<BookingResponseDTO>> call,
                                   Response<BaseResponse<BookingResponseDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<BookingResponseDTO> baseResponse = response.body();
                    if (baseResponse.isSuccess()) {
                        Toast.makeText(BookingDetailActivity.this, successMessage, Toast.LENGTH_SHORT).show();
                        finish(); // Return to list
                    } else {
                        showError(baseResponse.getMessage());
                    }
                } else {
                    showError("Không thể cập nhật đơn");
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<BookingResponseDTO>> call, Throwable t) {
                Log.e(TAG, "Error updating booking", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}