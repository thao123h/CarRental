package com.example.carrental.network.api;

import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.Category;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.modals.item.ItemDTO;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ItemService {
    //  Lấy tất cả items theo Category
    @GET("items")
    Call<BaseResponse<List<ItemDTO>>> getAllByCategory(@Query("category") Category category);

    // Lấy chi tiết 1 item theo id
    @GET("items/{id}")
    Call<BaseResponse<ItemDTO>> getItemById(@Path("id") Long id);

    // Lọc items theo địa chỉ
//    @GET("/filter")
//    Call<BaseResponse<List<ItemDTO>>> getAllItemsByAddress(@Query("address") String address);

    //  Lấy tất cả items của user hiện tại (người đang đăng nhập)
    @GET("items/me")
    Call<BaseResponse<List<ItemDTO>>> getAllMyItems();


    //  Tạo mới một xe ô tô
    @POST("cars")
    Call<BaseResponse<ItemDTO>> createCar(@Body CarDTO carDTO);



}
