package com.example.carrental.adapters;

import android.content.Context;
import android.util.Log;
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

import java.util.List;

public class CarAdapter extends RecyclerView.Adapter<CarAdapter.CarViewHolder> {

    private final Context context;
    private final List<CarDTO> carList;

    public CarAdapter(Context context, List<CarDTO> carList) {
        this.context = context;
        this.carList = carList;
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        CarDTO car = carList.get(position);

        Log.d("Adapter", "Bind xe: " + car.getBrand() + " " + car.getModel());

        holder.txtCarName.setText(car.getBrand() + " " + car.getModel());
        holder.txtCarPrice.setText("Biển số: " + (car.getLicensePlate() != null ? car.getLicensePlate() : "?"));

        if (car.getYear() != null) {
            holder.txtCarYear.setText("Năm: " + car.getYear());
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
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    public static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView txtCarName, txtCarPrice, txtCarYear;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCar = itemView.findViewById(R.id.imgCar);
            txtCarName = itemView.findViewById(R.id.txtCarName);
            txtCarPrice = itemView.findViewById(R.id.txtCarPrice);
            txtCarYear = itemView.findViewById(R.id.txtCarYear);
        }
    }
}
