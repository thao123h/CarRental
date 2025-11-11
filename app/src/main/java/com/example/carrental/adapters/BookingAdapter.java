package com.example.carrental.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.booking.BookingResponseDTO;
import com.example.carrental.modals.item.ItemImageDTO;
import com.example.carrental.utils.StatusHelper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView Adapter for displaying booking list
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private Context context;
    private List<BookingResponseDTO> bookingList;
    private OnBookingClickListener listener;

    // Date formatter for display
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public interface OnBookingClickListener {
        void onBookingClick(BookingResponseDTO booking);
    }

    public BookingAdapter(Context context, OnBookingClickListener listener) {
        this.context = context;
        this.bookingList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingResponseDTO booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void setBookingList(List<BookingResponseDTO> bookings) {
        this.bookingList = bookings != null ? bookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void clearBookings() {
        this.bookingList.clear();
        notifyDataSetChanged();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCarImage;
        TextView tvCarName;
        TextView tvStatus;
        TextView tvBookingDates;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCarImage = itemView.findViewById(R.id.ivCarImage);
            tvCarName = itemView.findViewById(R.id.tvCarName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvBookingDates = itemView.findViewById(R.id.tvBookingDates);

            // Set click listener
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onBookingClick(bookingList.get(position));
                }
            });
        }

        public void bind(BookingResponseDTO booking) {
            // Set car name
            if (booking.getItem() != null && booking.getItem().getName() != null) {
                tvCarName.setText(booking.getItem().getName());
            } else {
                tvCarName.setText("N/A");
            }

            // Load car image
            loadCarImage(booking);

            // Set status badge
            setStatusBadge(booking);

            // Set booking dates
            setBookingDates(booking);
        }

        private void loadCarImage(BookingResponseDTO booking) {
            String imageUrl = null;

            // Try to get image from item
            if (booking.getItem() != null) {
                // First try itemImages
                if (booking.getItem().getItemImages() != null &&
                        !booking.getItem().getItemImages().isEmpty()) {
                    ItemImageDTO firstImage = booking.getItem().getItemImages().get(0);
                    imageUrl = firstImage.getImageUrl();
                }
                // If no itemImages, try carDTO images
                else if (booking.getItem().getCarDTO() != null &&
                        booking.getItem().getCarDTO().getItemImages() != null &&
                        !booking.getItem().getCarDTO().getItemImages().isEmpty()) {
                    ItemImageDTO firstImage = booking.getItem().getCarDTO().getItemImages().get(0);
                    imageUrl = firstImage.getImageUrl();
                }
            }

            // Load image with Glide
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_car)
                        .error(R.drawable.ic_placeholder_car)
                        .centerCrop()
                        .into(ivCarImage);
            } else {
                // Use placeholder if no image
                ivCarImage.setImageResource(R.drawable.ic_placeholder_car);
            }
        }

        private void setStatusBadge(BookingResponseDTO booking) {
            // Get status text
            String statusText = StatusHelper.getStatusText(booking.getStatus());
            tvStatus.setText(statusText);

            // Get status color
            int statusColor = StatusHelper.getStatusColor(booking.getStatus());

            // Set background color for status badge
            GradientDrawable drawable = (GradientDrawable) tvStatus.getBackground();
            if (drawable != null) {
                drawable.setColor(statusColor);
            } else {
                tvStatus.setBackgroundColor(statusColor);
            }
        }

        private void setBookingDates(BookingResponseDTO booking) {
            try {
                String startDate = booking.getStartTime();
                String endDate = booking.getEndTime();
                String dateRange = String.format("From: %s → To: %s", startDate, endDate);
                tvBookingDates.setText(dateRange);
            } catch (Exception e) {
                tvBookingDates.setText("Date unavailable");
            }
        }
    }
}