package com.example.carrental.network.api;

import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.auth.JwtResponse;
import com.example.carrental.modals.auth.LoginRequest;
import com.example.carrental.modals.auth.SignupRequest;
import com.example.carrental.modals.auth.UserDTO;
import com.example.carrental.modals.booking.BookingRequestDTO;
import com.example.carrental.modals.booking.BookingResponseDTO;
import com.example.carrental.modals.booking.ScheduleDTO;
import com.example.carrental.modals.booking.UpdateBookingRequest;

import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface BookingService {

    // Lấy thông tin booking theo ID
    @GET("bookings/{id}")
    Call<BaseResponse<BookingResponseDTO>> getBookingById(@Path("id") Long id);

    //  Lấy tất cả booking của renter (người thuê)
    @GET("bookings/renter")
    Call<BaseResponse<BookingResponseDTO[]>> getAllBookingsByRenter();

    // Lấy tất cả booking theo ownerId (người cho thuê)
    @GET("bookings/owner/{ownerId}")
    Call<BaseResponse<BookingResponseDTO[]>> getAllBookingsByOwnerId(@Path("ownerId") Long ownerId);

    // Tạo booking mới
    @POST("bookings")
    Call<BaseResponse<BookingResponseDTO>> createBooking(@Body BookingRequestDTO bookingRequest);

    // Cập nhật booking
    @PUT("bookings/{id}")
    Call<BaseResponse<BookingResponseDTO>> updateBooking(
            @Path("id") Long id,
            @Body UpdateBookingRequest request
    );

    //  Xoá booking theo ID
    @DELETE("bookings/{id}")
    Call<BaseResponse<String>> deleteBooking(@Path("id") Long id);

    // Lấy danh sách schedule theo itemId
    @GET("bookings/schedule")
    Call<BaseResponse<List<ScheduleDTO>>> getAllBookingsBySchedule(@Query("itemId") Long itemId);

    //  Lấy danh sách itemId không khả dụng trong khoảng thời gian
}
