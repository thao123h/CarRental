package com.example.carrental.activities;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
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

    private static final String TAG = "AddCarActivity";

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
        viewPager.setUserInputEnabled(false); // disable swipe manually

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
                if (f instanceof PageFragment && !((PageFragment) f).validatePage()) {
                    Toast.makeText(this, "Vui lòng điền đủ thông tin ở bước này", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                        Toast.makeText(this, "Vui lòng kiểm tra lại thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        pf.putData(car);
                    } catch (Exception e) {
                        Log.e(TAG, "Error in putData at page " + i, e);
                        Toast.makeText(this, "Lỗi dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        viewPager.setCurrentItem(i);
                        updateButtons();
                        return;
                    }
                }
            }

            // Debug print JSON
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .create();
            Log.d(TAG, gson.toJson(car));

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

    public interface PageFragment {
        boolean validatePage();

        void putData(CarDTO car) throws Exception;
    }

    private static class FormPagerAdapter extends FragmentStateAdapter {

        private final List<Fragment> fragments;

        public FormPagerAdapter(@NonNull AppCompatActivity fa, List<Fragment> fragments) {
            super(fa);
            this.fragments = fragments;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

        public Fragment getFragmentAt(int pos) {
            return fragments.get(pos);
        }
    }

    public static class CarInfoFragment extends Fragment implements PageFragment {

        private TextInputEditText etPlate, etKms, carName, address;
        private Spinner spBrand, spYear, spSeat, spFuel;
        private RadioGroup rgTransmission;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_info, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPlate = v.findViewById(R.id.et_plate);
            etKms = v.findViewById(R.id.et_kms);
            spBrand = v.findViewById(R.id.spinner_brand);
            carName = v.findViewById(R.id.car_name);
            address = v.findViewById(R.id.et_address);
            spYear = v.findViewById(R.id.spinner_year);
            spSeat = v.findViewById(R.id.spinner_seat);
            spFuel = v.findViewById(R.id.spinner_fuel);
            rgTransmission = v.findViewById(R.id.rg_transmission);

            String[] brands = {"TOYOTA", "HONDA", "MAZDA"};
            spBrand.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, brands));

            int currYear = Calendar.getInstance().get(Calendar.YEAR);
            List<String> years = new ArrayList<>();
            for (int y = currYear; y >= 1980; y--) years.add(String.valueOf(y));
            spYear.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years));

            String[] seats = {"2", "4", "5", "7"};
            spSeat.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, seats));

            String[] fuels = {"PETROL", "DIESEL", "ELECTRIC"};
            spFuel.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fuels));
        }

        @Override
        public boolean validatePage() {
            try {
                if (etPlate.getText() == null || etPlate.getText().toString().trim().isEmpty()) return false;
                if (carName.getText() == null || carName.getText().toString().trim().isEmpty()) return false;
                if (address.getText() == null || address.getText().toString().trim().isEmpty()) return false;

                String kmsStr = etKms.getText() == null ? "" : etKms.getText().toString().trim();
                if (kmsStr.isEmpty()) return false;
                Integer.parseInt(kmsStr);

                if (spBrand.getSelectedItem() == null) return false;
                if (spYear.getSelectedItem() == null) return false;
                if (spSeat.getSelectedItem() == null) return false;
                if (spFuel.getSelectedItem() == null) return false;

                int checked = rgTransmission.getCheckedRadioButtonId();
                if (checked != R.id.rb_auto && checked != R.id.rb_manual) return false;

            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        }

        @Override
        public void putData(CarDTO car) throws Exception {
            car.setLicensePlate(etPlate.getText().toString().trim());
            car.setBrand(spBrand.getSelectedItem().toString());
            car.setFuelType(FuelType.valueOf(spFuel.getSelectedItem().toString()));
            car.setSeats(Integer.parseInt(spSeat.getSelectedItem().toString()));
            car.setYear(Integer.parseInt(spYear.getSelectedItem().toString()));
            try {
                car.setKms(Integer.parseInt(etKms.getText().toString().trim()));
            } catch (NumberFormatException e) {
                throw new Exception("Số km không hợp lệ");
            }

            int checked = rgTransmission.getCheckedRadioButtonId();
            if (checked == R.id.rb_auto) car.setTransmission(Transmission.AUTOMATIC);
            else if (checked == R.id.rb_manual) car.setTransmission(Transmission.MANUAL);
            else throw new Exception("Chưa chọn kiểu hộp số");

            if (car.getItem() == null) car.setItem(new ItemDTO());
            car.getItem().setName(carName.getText().toString().trim());
            car.getItem().setAddress(address.getText().toString().trim());
        }
    }

    public static class CarImagesFragment extends Fragment implements PageFragment {

        private TextInputEditText etUrl1, etUrl2, etUrl3, etUrl4;
        private ImageView ivUrl1, ivUrl2, ivUrl3, ivUrl4;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
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

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }

        @Override
        public boolean validatePage() {
            // Không bắt buộc nhập ảnh
            return true;
        }

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
                ItemImageDTO itemImageDTO = new ItemImageDTO();
                itemImageDTO.setImageUrl(et.getText().toString().trim());
                imgs.add(itemImageDTO);
            }
        }
    }

    public static class CarPriceFragment extends Fragment implements PageFragment {

        private TextInputEditText etPrice, etDeposit;
        private Switch switchNegotiable;

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_price, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPrice = v.findViewById(R.id.et_price_self);
            etDeposit = v.findViewById(R.id.et_deposit);
            switchNegotiable = v.findViewById(R.id.switch_negotiable);
        }

        @Override
        public boolean validatePage() {
            if (etPrice.getText() == null || etPrice.getText().toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập giá thuê", Toast.LENGTH_SHORT).show();
                return false;
            }
            try {
                Double.parseDouble(etPrice.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Giá thuê không hợp lệ", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        @Override
        public void putData(CarDTO car) throws Exception {
            if (car.getItem() == null) {
                car.setItem(new ItemDTO());
            }

            String priceStr = etPrice.getText() == null ? "" : etPrice.getText().toString().trim();
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                throw new Exception("Giá thuê không hợp lệ");
            }
            car.getItem().setPrice(price);

            String depositStr = etDeposit.getText() == null ? "" : etDeposit.getText().toString().trim();
            double deposit = 0.0;
            try {
                if (!depositStr.isEmpty()) deposit = Double.parseDouble(depositStr);
            } catch (NumberFormatException e) {
                throw new Exception("Tiền đặt cọc không hợp lệ");
            }
            car.getItem().setDepositAmount(deposit);

            // Nếu cần dùng switchNegotiable thì xử lý ở đây
            // car.getItem().setNegotiable(switchNegotiable.isChecked());
        }
    }
}
