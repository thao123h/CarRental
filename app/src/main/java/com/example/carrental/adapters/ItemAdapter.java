package com.example.carrental.adapters;



import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.activities.HomeActivity;
import com.example.carrental.modals.item.ItemDTO;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.VH> {
    private List<ItemDTO> list;
    private Context ctx;

    public ItemAdapter(Context ctx, List<ItemDTO> list, HomeActivity homeActivity) {
        this.ctx = ctx;
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(ctx).inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ItemDTO car = list.get(position);
        holder.tvCarName.setText(car.getName());
        holder.tvCarLocation.setText(car.getAddress());
        holder.tvCarPrice.setText(String.format("%,.0fđ/ngày", car.getPrice()));


        Glide.with(ctx)
                .load(car.getItemImages().get(0).getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgCar);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public void setData(List<ItemDTO> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    public interface OnItemActionListener {

    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgCar;
        TextView tvCarName, tvCarDetails, tvCarLocation, tvCarPrice;
        VH(@NonNull View v) {
            super(v);
            imgCar = v.findViewById(R.id.imgCar);
            tvCarName = v.findViewById(R.id.tvCarName);
            tvCarDetails = v.findViewById(R.id.tvCarDetails);
            tvCarLocation = v.findViewById(R.id.tvCarLocation);
            tvCarPrice = v.findViewById(R.id.tvCarPrice);
        }
    }
}
