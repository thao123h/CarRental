package com.example.carrental.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.carrental.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Gộp AddCarActivity và 4 Fragment (CarInfo, CarImages, CarDocuments, CarPrice) vào 1 file.
 *
 * Lưu ý:
 * - Các layout (R.layout.fragment_car_info, fragment_car_images, fragment_car_documents, fragment_car_price)
 *   và resource ids phải tồn tại trong project (theo cấu trúc bạn đã có).
 * - Không tạo package hoặc folder mới, file này để đặt vào thư mục activities.
 */
public class AddCarActivity extends AppCompatActivity {

    // đổi port/URL theo backend của bạn
    private static final String ADD_CAR_URL = "http://10.0.2.2:8082/item/addCar";

    private ViewPager2 viewPager;
    private Button btnPrev, btnNext;
    private FormPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_car);

        viewPager = findViewById(R.id.view_pager);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);

        List<Fragment> pages = new ArrayList<>();
        pages.add(new CarInfoFragment());
        pages.add(new CarImagesFragment());
//        pages.add(new CarDocumentsFragment());
        pages.add(new CarPriceFragment());

        adapter = new FormPagerAdapter(this, pages);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // tắt swipe nếu muốn điều khiển bằng nút

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
                if (f instanceof PageFragment) {
                    boolean ok = ((PageFragment) f).validatePage();
                    if (!ok) return;
                }
                viewPager.setCurrentItem(pos + 1);
                updateButtons();
            } else {
                // last page -> submit
                submitAll();
            }
        });
    }

    private void updateButtons() {
        int pos = viewPager.getCurrentItem();
        btnPrev.setEnabled(pos > 0);
        if (pos == adapter.getItemCount() - 1) btnNext.setText("Gửi");
        else btnNext.setText("Tiếp theo");
    }

    private void submitAll() {
        try {
            JSONObject carJson = new JSONObject();

            // gọi từng fragment để validate và thêm data vào carJson
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment f = adapter.getFragmentAt(i);
                if (f instanceof PageFragment) {
                    PageFragment pf = (PageFragment) f;
                    if (!pf.validatePage()) {
                        // validation failed; chuyển tới trang lỗi và abort
                        viewPager.setCurrentItem(i);
                        updateButtons();
                        return;
                    }
                    pf.putData(carJson);
                }
            }

            // gửi request bằng Volley
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, ADD_CAR_URL, carJson,
                    response -> {
                        Toast.makeText(this, "Thêm xe thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        error.printStackTrace();
                        Toast.makeText(this, "Lỗi khi thêm xe!", Toast.LENGTH_SHORT).show();
                    });

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Dữ liệu không hợp lệ!", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- Helper adapter để giữ tham chiếu fragment ----------
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

        // helper để lấy reference fragment (chúng ta lưu danh sách)
        public Fragment getFragmentAt(int pos) {
            return fragments.get(pos);
        }
    }

    // ---------- Interface cho các trang (fragment) ----------
    public interface PageFragment {
        // validate trang
        boolean validatePage();
        // đổ dữ liệu trang vào JSONObject carJson
        void putData(JSONObject carJson) throws Exception;
    }

    // ---------- Fragment 1: Thông tin xe ----------
    public static class CarInfoFragment extends androidx.fragment.app.Fragment implements PageFragment {

        private TextInputEditText etPlate, etKms, etItemTitle;
        private Spinner spinnerBrand, spinnerModel, spinnerYear, spinnerSeat, spinnerFuel;
        private RadioGroup rgTransmission;

        public CarInfoFragment() { super(); }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_info, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPlate = v.findViewById(R.id.et_plate);
            etKms = v.findViewById(R.id.et_kms);
            spinnerBrand = v.findViewById(R.id.spinner_brand);
            spinnerModel = v.findViewById(R.id.spinner_model);
            spinnerYear = v.findViewById(R.id.spinner_year);
            spinnerSeat = v.findViewById(R.id.spinner_seat);
            spinnerFuel = v.findViewById(R.id.spinner_fuel);

            rgTransmission = v.findViewById(R.id.rg_transmission);

            // sample data (bạn có thể thay bằng data thực)
            String[] brands = new String[]{"Chọn hãng", "BENTLEY", "TOYOTA", "HONDA"};
            String[] models = new String[]{"Chọn mẫu", "CONTINENTAL", "CAMRY", "CIVIC"};
            spinnerBrand.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, brands));
            spinnerModel.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, models));

            int curr = Calendar.getInstance().get(Calendar.YEAR);
            List<String> years = new ArrayList<>();
            years.add("Chọn năm");
            for (int y = curr; y >= 1980; y--) years.add(String.valueOf(y));
            spinnerYear.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, years));

            String[] seats = new String[]{"Chọn", "2", "4", "5", "7", "8"};
            spinnerSeat.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, seats));

            String[] fuels = new String[]{"Chọn fuel", "GASOLINE", "DIESEL", "ELECTRIC", "HYBRID"};
            spinnerFuel.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, fuels));
        }

        @Override
        public boolean validatePage() {
            String plate = etPlate.getText() == null ? "" : etPlate.getText().toString().trim();
            if (plate.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng nhập biển số", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        @Override
        public void putData(JSONObject carJson) throws Exception {
            String plate = etPlate.getText() == null ? "" : etPlate.getText().toString().trim();
            carJson.put("licensePlate", plate);

            String brand = spinnerBrand.getSelectedItem() == null ? "" : spinnerBrand.getSelectedItem().toString();
            String model = spinnerModel.getSelectedItem() == null ? "" : spinnerModel.getSelectedItem().toString();
            carJson.put("brand", brand);
            carJson.put("model", model);

            String yearStr = spinnerYear.getSelectedItem() == null ? "" : spinnerYear.getSelectedItem().toString();
            if (!yearStr.startsWith("Chọn") && !yearStr.isEmpty()) carJson.put("year", Integer.parseInt(yearStr));

            String seatsStr = spinnerSeat.getSelectedItem() == null ? "" : spinnerSeat.getSelectedItem().toString();
            if (!seatsStr.equals("Chọn") && !seatsStr.isEmpty()) carJson.put("seats", Integer.parseInt(seatsStr));

            int checked = rgTransmission.getCheckedRadioButtonId();
            if (checked == R.id.rb_auto) carJson.put("transmission", "AUTOMATIC");
            else if (checked == R.id.rb_manual) carJson.put("transmission", "MANUAL");

            String fuel = spinnerFuel.getSelectedItem() == null ? "" : spinnerFuel.getSelectedItem().toString();
            if (!fuel.startsWith("Chọn") && !fuel.isEmpty()) carJson.put("fuelType", fuel);

            String kmsText = etKms.getText() == null ? "" : etKms.getText().toString().trim();
            if (!kmsText.isEmpty()) {
                try {
                    carJson.put("kms", Integer.parseInt(kmsText));
                } catch (NumberFormatException ignored) { }
            }

            // item nested
            JSONObject item = new JSONObject();
            String itemTitle = etItemTitle.getText() == null ? "" : etItemTitle.getText().toString().trim();
            item.put("title", itemTitle);
            item.put("name", (brand + " " + model).trim());
            item.put("description", "Xe " + brand + " " + model);
            carJson.put("item", item);
        }
    }

    // ---------- Fragment 2: Ảnh xe ----------
    public static class CarImagesFragment extends androidx.fragment.app.Fragment implements PageFragment {

        private ImageView ivMain, ivSlot1, ivSlot2, ivSlot3, ivSlot4;
        private final List<String> imageUris = new ArrayList<>();
        private final ImageView[] slots = new ImageView[5];
        private int currentSlot = -1;

        private ActivityResultLauncher<String[]> openDocumentLauncher;

        public CarImagesFragment() { super(); }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            openDocumentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri result) {
                            if (result != null && currentSlot >= 0) {
                                try {
                                    requireContext().getContentResolver().takePersistableUriPermission(result,
                                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                                } catch (Exception ignored) { }
                                slots[currentSlot].setImageURI(result);
                                imageUris.set(currentSlot, result.toString());
                                currentSlot = -1;
                            }
                        }
                    });
        }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_images, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            ivMain = v.findViewById(R.id.iv_main);
            ivSlot1 = v.findViewById(R.id.iv_slot_1);
            ivSlot2 = v.findViewById(R.id.iv_slot_2);
            ivSlot3 = v.findViewById(R.id.iv_slot_3);
            ivSlot4 = v.findViewById(R.id.iv_slot_4);

            slots[0] = ivMain;
            slots[1] = ivSlot1;
            slots[2] = ivSlot2;
            slots[3] = ivSlot3;
            slots[4] = ivSlot4;

            // init imageUris list with 5 null entries
            imageUris.clear();
            for (int i = 0; i < 5; i++) imageUris.add(null);

            for (int i = 0; i < slots.length; i++) {
                final int s = i;
                slots[i].setOnClickListener(view -> {
                    currentSlot = s;
                    // open document for image/*, use OpenDocument to get persistable uri
                    openDocumentLauncher.launch(new String[]{"image/*"});
                });
            }
        }

        @Override
        public boolean validatePage() {
            // images optional - nếu muốn bắt buộc, kiểm tra ở đây
            return true;
        }

        @Override
        public void putData(JSONObject carJson) throws Exception {
            JSONArray imgs = new JSONArray();
            for (String s : imageUris) {
                if (s != null) imgs.put(s);
            }
            carJson.put("itemImages", imgs);
        }
    }

//

    // ---------- Fragment 4: Giá thuê ----------
    public static class CarPriceFragment extends androidx.fragment.app.Fragment implements PageFragment {

        private TextInputEditText etPriceSelfDrive, etPriceWithDriver;

        public CarPriceFragment() { super(); }

        @Nullable
        @Override
        public android.view.View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_car_price, container, false);
        }

        @Override
        public void onViewCreated(@NonNull android.view.View v, @Nullable Bundle savedInstanceState) {
            etPriceSelfDrive = v.findViewById(R.id.et_price_self);
        }

        @Override
        public boolean validatePage() {
            // bạn có thể bắt validate giá ở đây, ví dụ require price self-drive
            // String p = etPriceSelfDrive.getText() == null ? "" : etPriceSelfDrive.getText().toString().trim();
            // if (p.isEmpty()) { Toast.makeText(requireContext(), "Vui lòng nhập giá tự lái", Toast.LENGTH_SHORT).show(); return false; }
            return true;
        }

        @Override
        public void putData(JSONObject carJson) throws Exception {
            JSONObject item = carJson.has("item") ? carJson.getJSONObject("item") : new JSONObject();
            String pSelf = etPriceSelfDrive.getText() == null ? "" : etPriceSelfDrive.getText().toString().trim();
            String pWith = etPriceWithDriver.getText() == null ? "" : etPriceWithDriver.getText().toString().trim();
            if (!pSelf.isEmpty()) {
                try { item.put("price", Integer.parseInt(pSelf)); } catch (Exception ignored) { item.put("price", 0); }
            }
            if (!pWith.isEmpty()) {
                try { item.put("priceWithDriver", Integer.parseInt(pWith)); } catch (Exception ignored) { }
            }
            carJson.put("item", item);
        }
    }
}