package com.example.carrental.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.carrental.R;
import com.example.carrental.modals.BaseResponse;
import com.example.carrental.modals.enums.FuelType;
import com.example.carrental.modals.enums.Transmission;
import com.example.carrental.modals.item.CarDTO;
import com.example.carrental.modals.item.ItemDTO;
import com.example.carrental.modals.item.ItemImageDTO;
import com.example.carrental.network.RetrofitClient;
import com.example.carrental.network.api.ItemService;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCarActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnPrev, btnNext;
    private FormPagerAdapter adapter;
    private ItemService api;
    public static TextInputEditText  carName;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        api = RetrofitClient.createService(this, ItemService.class);

        viewPager = findViewById(R.id.view_pager);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        List<Fragment> pages = new ArrayList<>();
        pages.add(new CarInfoFragment());
        pages.add(new CarImagesFragment());
        pages.add(new CarPriceFragment());

        adapter = new FormPagerAdapter(this, pages);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        updateButtons();

        btnPrev.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos > 0) {
                viewPager.setCurrentItem(pos - 1);
                updateButtons();
            }
        });

        btnNext.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            if (pos < adapter.getItemCount() - 1) {
                Fragment f = adapter.getFragmentAt(pos);
                if (f instanceof PageFragment && !((PageFragment) f).validatePage()) return;
                viewPager.setCurrentItem(pos + 1);
                updateButtons();
            } else {
                submitAll();
            }
        });
    }

    private void updateButtons() {
        int pos = viewPager.getCurrentItem();
        btnPrev.setEnabled(pos > 0);
        btnNext.setText(pos == adapter.getItemCount() - 1 ? "Hoàn tất" : "Tiếp theo");
    }

    // ======================================
    // 🔹 Gửi dữ liệu lên server
    // ======================================
    private void submitAll() {
        try {
            CarDTO car = new CarDTO();
            car.setItem(new ItemDTO());
            car.setItemImages(new ArrayList<>());

            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment f = adapter.getFragmentAt(i);
                if (f instanceof PageFragment) {
                    PageFragment pf = (PageFragment) f;
                    if (!pf.validatePage()) {
                        viewPager.setCurrentItem(i);
                        updateButtons();
                        return;
                    }
                    pf.putData(car);
                }
            }

            api.createCar(car).enqueue(new Callback<BaseResponse<ItemDTO>>() {
                @Override
                public void onResponse(Call<BaseResponse<ItemDTO>> call, Response<BaseResponse<ItemDTO>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(AddCarActivity.this, "Thêm xe thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddCarActivity.this, "Không thể thêm xe (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<BaseResponse<ItemDTO>> call, Throwable t) {
                    Toast.makeText(AddCarActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    // ======================================
    // 🔹 Interface
    // ======================================
    public interface PageFragment {
        boolean validatePage();
        void putData(CarDTO car) throws Exception;
    }

    // ======================================
    // 🔹 Adapter
    // ======================================
    private static class FormPagerAdapter extends FragmentStateAdapter {
        private final List<Fragment> fragments;

        public FormPagerAdapter(@NonNull AppCompatActivity fa, List<Fragment> fragments) {
            super(fa);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) { return fragments.get(position); }

        @Override
        public int getItemCount() { return fragments.size(); }

        public Fragment getFragmentAt(int pos) { return fragments.get(pos); }
    }

    // ==========================================================
    // 🔹 Fragment 1: Thông tin xe
    // ==========================================================
    public static class CarInfoFragment extends Fragment implements PageFragment {
        public TextInputEditText etPlate, etKms;
        private android.widget.Spinner spBrand , spYear, spSeat, spFuel;
        private android.widget.RadioGroup rgTransmission;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                              @Nullable android.view.ViewGroup container,
                                              @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_info, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPlate = v.findViewById(R.id.et_plate);
            etKms = v.findViewById(R.id.et_kms);
            spBrand = v.findViewById(R.id.spinner_brand);
         carName = v.findViewById(R.id.car_name);
            spYear = v.findViewById(R.id.spinner_year);
            spSeat = v.findViewById(R.id.spinner_seat);
            spFuel = v.findViewById(R.id.spinner_fuel);
            rgTransmission = v.findViewById(R.id.rg_transmission);

            String[] brands = {"TOYOTA", "HONDA", "MAZDA"};
            spBrand.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, brands));



            int curr = Calendar.getInstance().get(Calendar.YEAR);
            List<String> years = new ArrayList<>();
            for (int y = curr; y >= 1980; y--) years.add(String.valueOf(y));
            spYear.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years));

            String[] seats = {"2", "4", "5", "7"};
            spSeat.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, seats));

            String[] fuels = {"GASOLINE", "DIESEL", "ELECTRIC"};
            spFuel.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fuels));
        }

        @Override
        public boolean validatePage() {
            return etPlate.getText() != null && !etPlate.getText().toString().trim().isEmpty();
        }

        @Override
        public void putData(CarDTO car) throws Exception {
            car.setLicensePlate(etPlate.getText().toString().trim());
            car.setBrand(spBrand.getSelectedItem().toString());

            car.setFuelType(FuelType.valueOf(spFuel.getSelectedItem().toString()));
            car.setSeats(Integer.parseInt(spSeat.getSelectedItem().toString()));
            car.setYear(Integer.parseInt(spYear.getSelectedItem().toString()));
            car.setKms(Integer.parseInt(etKms.getText().toString().trim()));

            int checked = rgTransmission.getCheckedRadioButtonId();
            if (checked == R.id.rb_auto)
                car.setTransmission(Transmission.AUTOMATIC);
            else if (checked == R.id.rb_manual)
                car.setTransmission(Transmission.MANUAL);
        }
    }

    // ==========================================================
    // 🔹 Fragment 2: Ảnh xe (nhập URL + preview tự động)
    // ==========================================================
    public static class CarImagesFragment extends Fragment implements PageFragment {
        private TextInputEditText etUrl1, etUrl2, etUrl3, etUrl4;
        private ImageView ivUrl1, ivUrl2, ivUrl3, ivUrl4;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                              @Nullable android.view.ViewGroup container,
                                              @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_images, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etUrl1 = v.findViewById(R.id.et_url1);
            etUrl2 = v.findViewById(R.id.et_url2);
            etUrl3 = v.findViewById(R.id.et_url3);
            etUrl4 = v.findViewById(R.id.et_url4);

            ivUrl1 = v.findViewById(R.id.iv_url1_preview);
            ivUrl2 = v.findViewById(R.id.iv_url2_preview);
            ivUrl3 = v.findViewById(R.id.iv_url3_preview);
            ivUrl4 = v.findViewById(R.id.iv_url4_preview);

            setupPreview(etUrl1, ivUrl1);
            setupPreview(etUrl2, ivUrl2);
            setupPreview(etUrl3, ivUrl3);
            setupPreview(etUrl4, ivUrl4);
        }

        private void setupPreview(TextInputEditText et, ImageView iv) {
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String url = s.toString().trim();
                    if (!url.isEmpty()) {
                        Glide.with(requireContext())
                                .load(url)
                                .placeholder(R.drawable.placeholder)
                                .error(android.R.drawable.ic_menu_report_image)
                                .into(iv);
                    } else {
                        iv.setImageResource(android.R.drawable.ic_menu_camera);
                    }
                }
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            });
        }

        @Override
        public boolean validatePage() { return true; }

        @Override
        public void putData(CarDTO car) {
            List<ItemImageDTO> imgs = new ArrayList<>();
            addUrl(imgs, etUrl1);
            addUrl(imgs, etUrl2);
            addUrl(imgs, etUrl3);
            addUrl(imgs, etUrl4);
            car.setItemImages(imgs);
        }

        private void addUrl(List<ItemImageDTO> imgs, TextInputEditText et) {
            if (et != null && et.getText() != null && !et.getText().toString().trim().isEmpty()) {
                imgs.add(new ItemImageDTO(null, et.getText().toString().trim(), null));
            }
        }
    }



    // 🔹 Fragment 4: Giá thuê
    // ==========================================================
    public static class CarPriceFragment extends Fragment implements PageFragment {
        private TextInputEditText etPrice;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater,
                                              @Nullable android.view.ViewGroup container,
                                              @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_price, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPrice = v.findViewById(R.id.et_price_self);
        }

        @Override
        public boolean validatePage() { return true; }

        @Override
        public void putData(CarDTO car) throws Exception {
            ItemDTO itemDTO = new ItemDTO();
            String p = etPrice.getText() == null ? "" : etPrice.getText().toString().trim();
            double price = 0.0;
            try { price = Double.parseDouble(p);
            itemDTO.setPrice(price);
            itemDTO.setName(carName.getText().toString());
            car.setItem(itemDTO);
            ;} catch (Exception ignored) {}
        }
    }
}
