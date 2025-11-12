package com.example.carrental.adapters;

import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.modals.item.ItemDTO;

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final Context context;
    private final List<ItemDTO> carList;
    private int longPressedPosition = -1; // Biến để lưu vị trí item được nhấn giữ

    public CarAdapter(Context context, List<ItemDTO> carList) {
        this.context = context;
        this.carList = carList;
    }

    // Getter và Setter cho vị trí
    public int getLongPressedPosition() {
        return longPressedPosition;
    }

    public void setLongPressedPosition(int longPressedPosition) {
        this.longPressedPosition = longPressedPosition;
    }

    // Getter để lấy danh sách xe từ Activity
    public List<ItemDTO> getCarList() {
        return carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        ItemDTO car = carList.get(position);

        if (car == null) {
            Log.e("Adapter", "ItemDTO ở vị trí " + position + " bị null");
            return;
        }

        if (car.getCarDTO() == null) {
            Log.e("Adapter", "CarDTO ở vị trí " + position + " bị null, id = " + car.getId());
            holder.txtCarName.setText("Dữ liệu xe không hợp lệ");
            holder.txtCarPrice.setText("");
            holder.txtCarYear.setText("");
            holder.imgCar.setImageResource(R.drawable.placeholder);
            return;
        }

        Log.d("Adapter", "Bind xe: " + car.getCarDTO().getBrand() + " " + car.getCarDTO().getModel());

        holder.txtCarName.setText(car.getCarDTO().getBrand() + " " +car.getCarDTO().getModel());
        holder.txtCarPrice.setText("Biển số: " + (car.getCarDTO().getLicensePlate() != null ? car.getCarDTO().getLicensePlate() : "?"));

        if (car.getCarDTO().getYear() != null) {
            holder.txtCarYear.setText("Năm: " + car.getCarDTO().getYear());
        } else {
            holder.txtCarYear.setText("Năm: ?");
        }

        if (car.getItemImages() != null && !car.getItemImages().isEmpty()) {
            String url = car.getItemImages().get(0).getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.imgCar);
            } else {
                holder.imgCar.setImageResource(R.drawable.placeholder);
            }
        } else {
            holder.imgCar.setImageResource(R.drawable.placeholder);
        }

        // Xử lý sự kiện nhấn giữ lâu
        holder.itemView.setOnLongClickListener(v -> {
            // Lưu lại vị trí của item được nhấn
            setLongPressedPosition(holder.getAdapterPosition());
            // Trả về false để sự kiện context menu được tiếp tục xử lý
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    // ViewHolder cần implement View.OnCreateContextMenuListener
    public static class CarViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        ImageView imgCar;
        TextView txtCarName, txtCarPrice, txtCarYear;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCar);
            txtCarName = itemView.findViewById(R.id.txtCarName);
            txtCarPrice = itemView.findViewById(R.id.txtCarPrice);
            txtCarYear = itemView.findViewById(R.id.txtCarYear);

            // Đăng ký context menu cho itemView
            itemView.setOnCreateContextMenuListener(this);
        }

        // Tạo menu (chúng ta sẽ định nghĩa các item trong Activity)
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // Dòng này rất quan trọng để activity có thể thêm item vào menu
            // Bạn có thể để trống hoặc thêm các item mặc định tại đây nếu muốn
        }
    }
}
